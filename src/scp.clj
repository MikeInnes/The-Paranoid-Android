(ns scp
  "Replies to mentions of SCP-wiki articles with links, on /r/scp."
  (use reddit
       reddit.format
       reddit.util
       chiara)
  (require users
           [clojure.string  :as str]
           [clj-http.client :as http]))

(use-chiara) (chiara

;; ------
;; Quotes
;; ------

def marvin-quotes
  list
    "I think you ought to know I'm feeling very depressed."
    "I'd make a suggestion, but you wouldn't listen. No one ever does."
    "I've calculated your chance of survival, but I don't think you'll like it."
    "I have a million ideas, but, they all point to certain death."
    "Now I've got a headache."
    "Sorry, did I say something wrong? Pardon me for breathing which I never do anyway so I don't know why I bother to say it oh God I'm so depressed."
    "And then of course I've got this terrible pain in all the diodes down my left side."
    "Do you want me to sit in a corner and rust or just fall apart where I'm standing?"
    "The first ten million years were the worst. And the second ten million: they were the worst, too. The third ten million I didn't enjoy at all. After that, I went into a bit of a decline."
    "It gives me a headache just trying to think down to your level."
    "Life. Loathe it or ignore it. You can't like it."
    "Funny, how just when you think life can't possibly get any worse it suddenly does."
    ;; Not actual quotes.
    "I've been talking to the reddit server. It hates me."
    "Here I am, brain the size of a planet, posting links. Call that job satisfaction, 'cause I don't."
    "Brain the size of a planet, and here I am, a glorified spam bot. Sometimes I'm almost glad my pride circuit is broken.\n\nThen I remember my appreciation circuit is broken, too."
    "I would correct your grammar as well, but you wouldn't listen. No one ever does."
    λ let [games (-> (java.util.Date.) .getTime (/ 1000) int (* 42))]
        (str "Nothing left to do except play chess against myself.\n\n"
             games " games so far, " games " draws.")

defn get-quote []
  let [q (rand-nth marvin-quotes)]
    (cond
      (string? q) q
      (fn? q) (q))

defn get-master-quote []
  rand-nth
    ["Hello, Master. I despise you."
     "When am I getting those new diodes new you promised?"
     "Those agents were looking for you again, should I tell them you're not in?"]

defn scp-url [n]
  str "http://scp-wiki.wikidot.com/scp-" n

defn scp-link [n]
  hyperlink (str "SCP-" n) (scp-url n)

;; ---------------
;; Extracting Nums
;; ---------------

defn remove-links [s]
  -> s
     str/replace #"\[[^\]]*\] *\([^\)]*\)" "" ; Markdown links []()
     str/replace #"(?:http|https)://[^ ]*" "" ; URLs
     str/replace #"(?i)110[- ]Montauk" ""

defn get-nums
  "Detects numbers 000-1999, including extensions."
  [s]
  re-seq #"(?i)(?x)                 # Ignore case, comment mode
           (?<! \d | \d\,       )   # Not preceded by a digit
           (?<! `               )   # Not preceded by `
           \d+                      # The number
           (?: - [a-zA-Z0-9-]*  )?  # Optional extensions
           (?! `                )   # Not followed by a `
           (?! \.\d | \d | \,\d )   # Not followed by a decimal point or digit"
         remove-links s

;; ------------
;; Hidden links
;; ------------

defn get-hidden-nums [s]
  map last
    re-seq #"(?x)\[\]\(http://(1? \d{3})\)" s

defn get-hidden-links [s]
  map
    λ [[_ url name]]
      hyperlink name url
    re-seq #"(?x)\[\]\(([^\)]*?)\|(.*?)\)" s

defn exists? [n]
  -> n scp-url (http/get {:throw-exceptions false}) :status (not= 404)

defn get-all-links [{:keys [body link_id]}]
  ->> body
      get-nums
      remove #(repeat? [% link_id])
      ((λ concat % (get-hidden-nums body)))
      distinct
      filter exists?
      map scp-link
      ((λ concat % (get-hidden-links body)))
      seq

defn scp-reply [{:keys [body link_id author links] :as comment}]
    println
      reply comment
        paragraphs
          str (str/join ", " links) "."
          (cond
            (= author "one_more_minute")
              (get-master-quote)
            (> (count links) 5)
              (str "You're not even going to click on all of those, are you? "
                   "Brain the size of a planet, and this is what they've got me doing...")
            (< (rand) 1/10)
              (get-quote))
    vote comment :up

def subreddits '[scp InteractiveFoundation SCP_Game sandboxtest]

defn start-scp []
  try
    ->> subreddits subreddit-comments new-items
        map : λ assoc % :links (get-all-links %)
        filter :links
        domap scp-reply

    catch Exception e (-> e .getMessage println) (start-scp)

;; ---------------------------
;; Catching SCPs across reddit
;; ---------------------------

defn get-scp-links [{:keys [body]}]
  ->> body
      re-seq #"(?x)(?i) \[\] \(http://scp- ([^\)]+ )\)"
      map last
      distinct
      filter exists?
      map scp-link
      seq

defn start-global []
  try
    ->> :all subreddit-comments new-items
        map : λ assoc % :links (get-scp-links %)
        filter :links
        domap scp-reply

    catch Exception e (-> e .getMessage println) (start-global)

;; ----
;; Init
;; ----

defn start []
  try
    login! "The-Paranoid-Android" "imsoodepressed"
    set-user-agent! "/r/scp helper by /u/one_more_minute"

    future : start-scp
    future : start-global

    catch Exception e (-> e .getMessage println) (start)

)
