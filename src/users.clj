(ns users
  (:use reddit))

(def marvin
  (reddit/login "The-Paranoid-Android" "imsoodepressed"))

(def askreddit
  (reddit/login "Ask-Reddit-Bot" "askmeanything"))