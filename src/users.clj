(ns users
  (:require reddit))

(defmacro defusers
  "Define multiple logins at once.
  e.g.
  `(defusers
     account1 \"account-name\" \"password\"
     account2 \"account-name\" \"password\")`
  expands to 
  `(do
     (def account1 (login \"account-name\" \"password\"))
     (def account2 (login \"account-name\" \"password\")))`"
  [& users]
  (let [users (partition 3 users)]
    `(do
       ~@(map (fn [[sym user pass]]
                `(def ~sym (reddit/login ~user ~pass)))
              users))))

(defusers
  marvin      "The-Paranoid-Android" "imsoodepressed"
  askreddit   "Ask-Reddit-Bot"       "askmeanything"
  top-comment "Top-Comment-Bot"      "insertwittyresponse")
