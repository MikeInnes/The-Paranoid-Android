(ns bots
  (:require [monger.core :as mg]
             robbit)
  (:use scp karma-police))

(defn mongo-connect! []
  (if-let [url (System/getenv "MONGOHQ_URL")]
    (mg/connect-via-uri! url)
    (do
      (mg/connect!)
      (mg/set-db! (mg/get-db "local")))))

(defn start []
  (robbit/start scp-bot      :scp-bot)
  (robbit/start karma-police :karma-police))

(defn stop []
  (robbit/stop :scp-bot)
  (robbit/stop :karma-police))
