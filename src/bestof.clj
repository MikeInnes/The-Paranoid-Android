(ns bestof
  (:use reddit)
  (:require [clojure.string :as str]))

;; ------------------------------------------
;; Wilson Scoring
;; Entirely useless when working with reddit,
;; due to vote fuzzing.
;; ------------------------------------------

;; z = -Sqrt[2] InverseErfc[1 + c]
;; c = 0.95, z = 1.96

(def z 1.96)

(defn wilson [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (+ p (* (/ 1 2) (Math/pow n -1) (Math/pow z 2))) (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1))))

(defn wilson-upper [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1) (+ p (* (/ 1 2) (Math/pow n -1) (Math/pow z 2)) (* z (Math/pow (+ (* (Math/pow n -1) (+ 1 (* -1 p)) p) (* (/ 1 4) (Math/pow n -2) (Math/pow z 2))) (/ 1 2)))))))

(defn wilson-lower [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1) (+ p (* (/ 1 2) (Math/pow n -1) (Math/pow z 2)) (* -1 z (Math/pow (+ (* (Math/pow n -1) (+ 1 (* -1 p)) p) (* (/ 1 4) (Math/pow n -2) (Math/pow z 2))) (/ 1 2)))))))

(defn assoc-wilson [{:keys [ups downs] :as item}]
  (assoc item :wilson (wilson ups downs)))

;; -----------
;; Utility fns
;; -----------

(defn pow [n p]
  (Math/pow n p))

(defn log [b n]
  (inc (/ (Math/log (max n 1))
          (Math/log b))))

(defn word-count [thing]
  (-> thing :body (str/split #" ") count))

(defn avg [xs]
  (if (empty? xs)
    1
    (/ (apply + xs)
       (count xs))))

(defn geom-mean [xs]
  (if (empty? xs)
    1
    (pow (apply * xs) (/ (count xs)))))

;; ------------------
;; Discussion scoring
;; ------------------

(declare d-score)

(defn replies-d-score [thing]
  (->> thing :replies (filter comment?) (map d-score) avg))

(defn d-score
  ""
  [thing]
  (avg [(word-count thing) (replies-d-score thing)]))

;; The word count of a self post is not factored in,
;; so that other links are not disadvantaged.

(defn score
  ""
  [link]
  (let [base-score (replies-d-score link)]
    (geom-mean [base-score base-score
                (log 10 (link :score))
                (log 10 (-> link :replies count))])))

(defn assoc-score [link]
  (assoc link :d-score (score link)))

;; ------------
;; Bestof lists
;; ------------

(defn items-with-replies [url]
  (->> url items (pmap #(api-call (with-replies %))) (take 200) doall))

(defn sort-best [items]
  (->> items
       (map assoc-score)
       (sort-by :d-score >)))

(defn best-list [items]
  (let [best     (sort-best items)
        avg      (avg (take 50 (map :d-score best)))
        best-str (->> best
                      (map #(str (identity (:d-score %)) " " (:title %) "\n" (:permalink %)))
                      (str/join "\n\n"))]
    (spit "data/best.txt" (str "Subreddit score: " avg "\n\n" best-str))))

(defn link-list [items]
  (let [best     (sort-best items)
        best-str (->> best
                      (map #(str "1. [" (% :title) "](" (% :permalink) ")"))
                      (str/join "\n\n"))]
    (spit "data/best.txt" best-str)))

;; -----------------
;; Testing functions
;; -----------------

(defn time-taken
  "Calculate how much time a call for n best items
  will take."
  [n]
  (let [link-page-reqs  (inc (quot (dec n) 100))
        reply-reqs      n
        total           (+ link-page-reqs reply-reqs)
        secs            (* (- total 1) 2)
        mins            (/ secs 60)
        hours           (/ mins 60)]
    (str secs " secs or "
         (if (> hours 1)
           (str (float hours) " hours.")
           (str (float mins)  " mins.")))))

(defn score-from-url [url]
  (score (with-replies {:kind :link
                        :permalink url})))

(defmacro defreddit [sym]
  `(->> ~(name sym) subreddit items-with-replies (def ~sym)))

(defmacro savereddit [sym]
  `(spit (str "data/" ~(name sym)) (pr-str ~sym)))

(defmacro loadreddit [sym]
  `(->> ~(name sym) (str "data/") slurp read-string (def ~sym)))
