(ns robbit
  "A framework for running reddit bots."
  (:use      robbit.log robbit.response
            [reddit :exclude (author?)]
             util.spacers)
  (:require [reddit.url :as url]))

;; NEEDS TO BE DELETED AND REWRITTEN COMPLETELY

(defn- now [] (java.util.Date.))

(defonce bots (atom {}))

(defn- default-bot
  "A bot that doesn't do anything. Custom bots 'extend'
  this object."
  []
  {:user-agent   "Unnamed bot made with robbit."  ; Short description + main account username.
   :type         :comment                         ; :comment/:link - choose which to respond to.
   :handler      (fn [_] nil)                     ; fn to take a comment object and return a response map.
   :subreddits   "all"                            ; String or vector of strings. Load comments/links from here.
   :login        nil                              ; Use reddit/login to generate a login.
   :interval     5                                ; Minutes between successive runs.
   :last-run     (now)                            ; Date in the format "yyyy-MM-dd HH:mm:ss".
   :debug        false                            ; Responses will be logged but not actually performed.
   :log          log
   :cancelled    (atom false)})                   ; Internal: the bot's thread will check this.

(defn init-bot
  "Merge with the default bot and get ready to run."
  [bot key] (-> (default-bot)
                (merge bot)
                (assoc :key key)
                (update-in [:last-run] atom)))

(defn- author? [thing bot]
  (= (thing :author) (-> bot :login :name)))

(defn update-last-run [bot date]
  (swap! (bot :last-run) #(if (.after date %) date %)))

(defn bot-items [bot]
  (->> (bot :subreddits)
       ((if (= (bot :type) :link) url/subreddit-new
                                  url/subreddit-comments))
       (items-since @(bot :last-run))
       (filter #(not (author? % bot)))))

;; ------------------------
;; User-friendly functions.
;; ------------------------

(defn run-once [bot]
  (with-user-agent (bot :user-agent)
    ((bot :log) (bot :key) " running: " (now))
    (let [items     (bot-items bot)
          responses (map (bot :handler) items)]
      (map' (fn [item response-map]
              (map' (fn [[type data]]
                      (handle-response bot (with-meta item {:response-type type}) data))
                    response-map))
            items responses))))

(defn run-bot [bot]
  (let [spacer (spacer (* (bot :interval) 60 1000))]
    (loop []
      (spaced spacer
        (when-not @(bot :cancelled)
          (try (run-once bot)
            (catch Exception e ((bot :log) (str "Error running " (bot :key) "\n" e))))
          (recur))))))

(defn stop [key]
  (reset! (-> key (@bots) :cancelled) true)
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
      (println "Login for" key "invalid:" (str "\n" (bot :login))))))
