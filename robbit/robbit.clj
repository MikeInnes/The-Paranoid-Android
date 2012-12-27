(ns robbit
  "A framework for running reddit bots."
  (:use      robbit.log robbit.response
             reddit
             util.spacers)
  (:require [reddit.url :as url])
  (:import java.util.Calendar
           java.util.Date))

(defn- now [] (java.util.Date.))
(def ^:private map' (comp dorun pmap))

(defonce ^:private bots (atom {}))

(defn- default-bot
  "A bot that doesn't do anything. Custom bots 'extend'
  this object."
  []
  {:user-agent   "Unnamed bot made with robbit."  ; Short description + main account username.
   :type         :comment                         ; :comment/:link - choose which to respond to.
   :handler      (fn [_] nil)                     ; fn to take a comment/link and return a response map.
   :subreddits   "all"                            ; String or vector of strings. Load comments/links from here.
   :login        nil                              ; Use reddit/login to generate a login.
   :interval     5                                ; Minutes between successive runs.
   :last-run     (now)                              ; Date
   :delay        0
   :cancelled    (atom false)})                   ; Internal: the bot's thread will check this.

(defn- update-last-run [bot date]
  (swap! (bot :last-run) #(if (.after date %) date %)))

(defn- subtract-mins [date mins]
  (let [cal (Calendar/getInstance)]
    (.setTime cal date)
    (.add cal Calendar/MINUTE (- mins))
    (.getTime cal)))

(defn- bot-items [{:keys [subreddits type last-run delay] :as bot}]
  (->> subreddits
       ((condp = type
          :link    url/subreddit-new
          :comment url/subreddit-comments))
       (items-since @last-run)
       (filter #(.before (:time %) (subtract-mins (Date.) delay)))))

;; ------------------------
;; User-friendly functions.
;; ------------------------

(defmacro debug [& forms]
  `(binding [*debug* true]
     ~@forms))

(defmacro with-log [log & forms]
  `(binding [*log* ~log]
     ~@forms))

(defn init-bot
  "Merge with the default bot and get ready to run."
  [bot key] (-> (default-bot)
                (merge bot)
                (assoc :key key)
                (assoc-in [:last-run] (atom (or (bot :last-run)
                                                (subtract-mins (now) (get bot :delay 0)))))))

(defn run-once [{:keys [key handler user-agent retry] :as bot}]
  (with-user-agent user-agent
    (let [items     (bot-items bot)
          responses (pmap handler items)]
      (*log* key " running " (count items))
      (map' (fn [item response-map]
              (if-not retry (update-last-run bot (item :time)))
              (map' (fn [[type data]]
                      (update-last-run bot (item :time))
                      (handle-response bot (with-meta item {:response-type type}) data))
                    response-map))
            items responses))))

(defn run-bot [{:keys [key interval cancelled] :as bot}]
  (let [spacer (spacer (* interval 60 1000))]
    (loop []
      (spaced spacer
        (when-not @cancelled
          (try (run-once bot)
            (catch Exception e (*log* (str "Error running " key "\n" e))))
          (recur))))))

(defn stop [key]
  (reset! (-> @bots key :cancelled) true)
  (swap! bots dissoc key))

(defn start
  ([bot] (start bot (-> bot :login :name)))
  ([bot key]
    (if (login-success? (bot :login))
      ; Start the bot
      (let [bot (init-bot bot key)]
        (if (@bots key) (stop key))
        (future (run-bot bot))
        (swap! bots assoc key bot))
      ; Or don't
      (*log* "Login for" key "invalid:" "\n" (pr-str (bot :login))))))
