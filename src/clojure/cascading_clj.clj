(ns cascading-clj-2)

(defmacro cfn [init-expr & body]
  "Read a str'ed clojure value, expect init-expr to be a binding form. Bind
  the value to the init-expr. Execute body, and prnstr the resultant value."
  `(fn [s#] 
     (when-let [result#
                (let [obj# (read-string s#)]
                  (apply (fn [~@init-expr] ~@body) obj#))]
       (pr-str result#))))

(defmacro defcfn [nm init-expr &body]
  "Convenience for naming a cfn'ed body."
  `(def ~nm (cfn ~init-expr ~@body)))

(defn- unpack-tuple [t]
  (.. t getArguments getTuple (getString t 0)))

(defn- pack-tuple [s]
  (Tuple. s))

(defn each-cfn [f]
  "Wraps the single cfn'ed expression around tuple reading and writing, and implementing
  the proxy interface."
  (proxy [BaseOperation Function] []
    (operate [flow call] 
             (let [argstr (unpack-tuple call)]
               (when-let [result (f argstr)]
                   (let [result-tuple (pack-tuple result)
                         out (.getOutputCollector call)]
                     (.add out result-tuple)))))))

(defn chain [lhs fs]
  "Takes a sequence of cfn's and returns a function that takes the head of a pipe assembly,
  and returns the result of applying Each function to it, which is another pipe assembly."
  (let [pipeline (reduce (fn [pipe f] (Each. #^Pipe pipe Fields/FIRST (each-cfn f))) lhs fs)]
    (cap (importer pipeline))))
