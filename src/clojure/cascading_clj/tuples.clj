(ns cascading-clj.tuples
  (:require [cascading-clj.imports :as imp]))

(imp/import-all)

(defn fields [names]
  "Constructs a new Fields instance from the sequence, or string, given as 'names'.
  Item(s) in names will be coerced to strings."
  (if (sequential? names)
    (Fields. (into-array (map #(str %) names)))
    (Fields. (str names))))

(defn apply-pipe
  "Returns a function that accept one argument, a Cascading Pipe, and returns a new Each pipe that will apply the given Cascading Function to it."
  ([fun select-fields output-fields] (fn [lhs] (Each. #^Pipe lhs #^Fields select-fields #^Function fun #^Fields output-fields)))
  ([fun select-fields] (apply-pipe fun select-fields Fields/UNKNOWN))
  ([fun] (apply-pipe fun Fields/ARGS Fields/UNKNOWN)))

(defn filter-pipe 
  "Like apply-pipe, but applies the filter operation to the given pipe."
  ([fun select-fields] (fn [lhs] (Each. #^Pipe lhs #^Fields select-fields #^Filter fun)))
  ([fun] (fn [lhs] (Each. #^Pipe lhs #^Filter fun))))

(defn chain [& pipes]
  "Chain a sequence of assembly operations together. Yields a function that takes one argument,
  the lhs pipe, and applies the sequence of assembly operations to it, yielding the resultant
  tuple stream."
  (apply comp (reverse pipes)))

;; I'm not sure why this has to be a macro; at runtime it's not available as a function.
(defmacro tupleseq [t]
  "Turn a Cascading Tuple into a sequence of its arguments (Tuple is iterable)."
  `(seq (.. ~t getArguments getTuple)))

(defn cfn [fun]
  "Take a function which accepts one argument, a sequence of tuple values, and returns a sequence of resulting tuple 
  values (as anything sequential). Produce a proxy object
  that can be used as a Cascading capital-F Function for apply-pipe, which will apply the argument and write any resulting tuples to the output stream."
  (proxy [BaseOperation Function] []
    (operate [flow call] 
             (let [args (tupleseq call)
                   out (.getOutputCollector call)]
               (when-let [results (fun args)]
                 (.add out (Tuple. (into-array results))))))))

(defmacro mktuple [ks kvs]
  "Take the key-value mapping kvs, and, for each matching key in ks, take the resultant value from kvs. Return a Tuple of the assembled values."
  `(Tuple. (into-array (map (fn [k#] (get ~kvs (keyword k#))) ~ks))))

(defn keyword-cfn [fieldnames fun]
  "Take a list of field names and a function which accepts a sequence of tuple values (as an argument) and returns a sequence of key-value mappings. 
  Produce a proxy object that can be used a cascading Function, which will apply the argument function, and write to the resulting stream
  only those values from the mappings whose keys exist in the field names list. You can use this to write functions that return named values."
  (let [fieldlist (fields fieldnames)
        fields-strings (into-array fieldnames)]
    ;; An important thing I found here was to convert the fieldnames collection into a Java type here. Cascading will attempt to serialize
    ;; Function objects, and Clojure's PersistentVector$Node is not Serialiable. If you bind a vector as an argument here to the proxy class,
    ;; your job will fail at runtime with a NotSerializableException.
    (proxy [BaseOperation Function] [fieldlist]
      (operate [flow call] 
               (let [args (tupleseq call)
                     out (.getOutputCollector call)]
                 (when-let [results (fun args)]
                   (.add out (mktuple fields-strings results))))))))

(defmacro defcfn [nm args & body]
  "Define a new proxy object that implements Function and wraps the equivalent clojure function. A convenience macro around 'cfn' to make writing
  cascading wrapped functions more natural."
  `(def ~nm (cfn (fn ~args ~@body))))

(defmacro defkeyword-cfn [nm args fieldnames & body]
  "Define a new proxy object that implements Function and wraps the equivalent clojure function. A convenience macro around 'keyword-cfn'."
  `(def ~nm (keyword-cfn (fn ~args ~@body) ~fieldnames)))

(defn cfilter [pred]
  "Take a predicate that will accept a sequence of tuple values. Return a cascading Filter
  that will apply the predicate to an incoming tuple stream."
  (proxy [BaseOperation Filter] []
    (isRemove [flow call] 
             (let [args (tupleseq call)]
               (boolean (pred args))))))

(defmacro deffilter [nm args & body]
  "Define a new proxy object that implements Filter and wraps the equivalent clojure function. A convenience macro around 'cfilter'."
  `(def ~nm (cfilter (fn ~args ~@body))))

(defn print-pipe []
  "A debugging filter that will prn the sequence of tuple values received as they stream from the source pipe."
  (filter-pipe (cfilter (fn [x] (prn x) true))))
