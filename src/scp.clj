(ns scp
  "Replies to mentions of SCP-wiki articles with links, on /r/scp."
  (:use reddit.format [clarity core utils])
  (:require  users
            [clojure.string  :as str]
            [clj-http.client :as http]))

(use-clarity)
(clarity

def marvin-quotes
  list
    "I think you ought to know I'm feeling very depressed."
    "I'd make a suggestion, but you wouldn't listen. No one ever does."
    ; "I've been talking to the main computer. It hates me."
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
    "Brain the size of a planet, and here I am, a glorified spam bot. Sometimes I'm almost glad my pride circuit is broken.\n\nThen I remember my appreciation circuit is broken too."
    "I would correct your grammar as well, but you wouldn't listen. No one ever does."
    λ let [games (-> (java.util.Date.) .getTime (/ 1000) int (* 42))]
        (str "Nothing left to do except play chess. Against myself.\n\n"
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
     "The Foundation were looking for you again, should I tell them you're not in?"]

defn scp-url [n]
  str "http://scp-wiki.wikidot.com/scp-" n

defn scp-link [n]
  hyperlink (str "SCP-" n) (scp-url n)

defn exists? [n]
  -> n scp-url (http/get {:throw-exceptions false}) :status (not= 404)

defn remove-brackets [s]
  loop [s' ""
        [first & rest] (map str s)
        brackets 0]
    (cond
      (nil? first)       s'
      (#{"(" "["} first) (recur s' rest (inc brackets))
      (#{")" "]"} first) (recur s' rest (dec brackets))
      (> brackets 0)     (recur s' rest brackets)
      :else              (recur (str s' first) rest brackets))

defn get-nums
  "Detects numbers 000-1999, including extensions."
  [s]
  re-seq #"(?i)(?x)              # Ignore case, comment mode
           (?<! \d           )   # Not preceded by a digit
           (?<! `            )   # Not preceded by `
           1? \d{3}              # 000 - 1999
           (?: -EX|-ARC|-J|-D)?  # Optional extensions
           (?! `             )   # Not followed by a `
           (?! \.\d | \d     )   # Not followed by a decimal point or digit"
         remove-brackets s

defn probably
  "True with probability n."
  [n]
  < (rand) n

def replies : atom [false {}]

defn repeat? [number link]
  first
    swap! replies
      λ [[_ links]]
        if-let [numbers (links link)]
          if (contains? numbers number)
            [true links]
            [false (assoc links link (conj numbers number))]
          [false (assoc links link #{number})]

defn scp-reply [{:keys [body link_id author]}]
  when-let [nums (->> body get-nums distinct (filter exists?) (remove #(repeat? % link_id)) seq)]
    {:reply (paragraphs
              (str (str/join ", " (map scp-link nums)) ".")
                (cond
                  (= author "one_more_minute")
                    (get-master-quote)
                  (> (count nums) 5)
                    (str "You're not even going to click on all of those, are you? "
                         "Brain the size of a planet, and this is what they've got me doing...")
                  (probably 1/10)
                    (get-quote)))
     :vote  :up}

def scp-bot
  {:handler      scp-reply
   :user-agent   "/r/scp helper by /u/one_more_minute"
   :subreddits   ["scp" "InteractiveFoundation" "SCP_Game"]
   :login        users/marvin
   :interval     0.5
  }

)
