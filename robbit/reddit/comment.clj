(ns reddit.comment)

(defn quotify [& s]
  (clojure.string/replace (apply str s) #"(\A|\n\n)(.+)" "$1> $2"))

(defn superscript [& s]
  (clojure.string/replace (apply str s) #"([^ \[*]+)" "^$1"))

(defn link [s url]
  (str "[" s "](" url ")"))