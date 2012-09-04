(ns users
  (:use reddit))

(defmacro defusers [& users]
  (let [users (partition 3 users)]
    `(do
       ~@(map (fn [[sym user pass]]
                `(def ~sym (login ~user ~pass))) users))))

(defusers
  marvin    "The-Paranoid-Android" "imsoodepressed"
  askreddit "Ask-Reddit-Bot"       "askmeanything")