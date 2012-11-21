(ns reddit.comment
  "Functions for working with reddit comment objects,
  and formatting strings as comments.
  Functions that apply to links as well (e.g. `reply`)
  are contained in `reddit.link`.")

;; ----------
;; Formatting
;; ----------

(defn quotify
  "Format the text as a markdown quote."
  [& s]
  (clojure.string/replace (apply str s) #"(\A|\n\n)(.+)" "$1> $2"))

(defn superscript
  "^Superscript each word."
  [& s]
  (clojure.string/replace (apply str s) #"([^ \[*]+)" "^$1"))

(defn hyperlink
  "Create markdown link."
  [s url]
  (str "[" s "](" url ")"))

(defn italic
  "Wrap the given text in asterisks."
  [& s]
	(str "*" (apply str s) "*"))
