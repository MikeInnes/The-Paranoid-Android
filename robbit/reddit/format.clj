(ns reddit.format
  (:require [clojure.string :as str]))

(defn quotify
  "Format the text as a markdown quote, i.e.
  each new paragraph beginning with '> '."
  [& s]
  (clojure.string/replace (apply str s) #"(\A|\n\n)(.+)" "$1> $2"))

(defn superscript
  "^Superscript each word."
  [& s]
  (str/replace (apply str s) #"([^ \[*]+)" "^$1"))

(defn superscript-n [n & s]
  (nth (iterate superscript (apply str s)) n))

(defn hyperlink
  "Create markdown link."
  [s url]
  (str "[" s "](" url ")"))

(defn italic
  "Wrap the given text in asterisks."
  [& s]
	(str "*" (apply str s) "*"))

(defn paragraphs
  "Create a comment with each argument
  as a seperate paragraph."
  [& ss] (->> ss (filter identity) (str/join "\n\n")))

(def line "------------------")
