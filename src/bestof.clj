(ns bestof
  (:use reddit)
  (:require [clojure.string :as str]))

;; z = -Sqrt[2] InverseErfc[1 + c]
;; c = 0.95, z = 1.96

(def z 1.96)

(defn wilson [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (+ p (* (/ 1 2) (Math/pow n -1) (Math/pow z 2)))
       (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1))))

(defn wilson-upper [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1)
       (+ p
          (* (/ 1 2) (Math/pow n -1) (Math/pow z 2))
          (* z (Math/pow (+ (* (Math/pow n -1) (+ 1 (* -1 p)) p) (* (/ 1 4) (Math/pow n -2) (Math/pow z 2))) (/ 1 2)))))))

(defn wilson-lower [ups downs]
  (let [n (+ ups downs)
        p (/ ups n)]
    (* (Math/pow (+ 1 (* (Math/pow n -1) (Math/pow z 2))) -1) 
       (+ p (* (/ 1 2) (Math/pow n -1) (Math/pow z 2)) 
       (* -1 z (Math/pow (+ (* (Math/pow n -1) (+ 1 (* -1 p)) p) (* (/ 1 4) (Math/pow n -2) (Math/pow z 2))) (/ 1 2)))))))

(defn assoc-wilson [{:keys [ups downs] :as item}]
  (assoc item :wilson (wilson ups downs)))

(defn bestof []
  (->> (subreddit-links ["bestof" "defaultgems"])
       items
       (map assoc-wilson)
       (sort-by :wilson >)))

(defn bestof-list []
  (->> (bestof)
       (map #(str (:wilson %) " " (:title %) "\n" (:permalink %)))
       (str/join "\n\n")
       (spit "best.txt")))
