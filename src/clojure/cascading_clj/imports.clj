(ns cascading-clj.imports
  (:require [cascading-clj.imports :as imp]))

(defn import-tap []
  (import '(cascading.tap Tap SourceTap SinkTap Hfs Dfs MultiSourceTap SinkMode)))

(defn import-flow []
  (import '(cascading.flow Flow FlowConnector FlowProcess)))

(defn import-tuple []
  (import '(cascading.tuple Tuple TupleEntry Fields)))

(defn import-pipes []
  (import '(cascading.pipe Each Every Pipe SubAssembly Group GroupBy)))

(defn import-regex-ops []
  (import '(cascading.operation.regex RegexFilter RegexMatcher)))

(defn import-ops []
  (do 
    (import '(cascading.operation Function Buffer Filter Operation BaseOperation Aggregator 
                                  FunctionCall))
    (import-regex-ops)))

(defn import-scheme []
  (import '(cascading.scheme Scheme TextLine TextDelimited)))

(defn import-all []
  (do 
    (import-tap) 
    (import-tuple)
    (import-flow) 
    (import-pipes) 
    (import-scheme) 
    (import-ops)))
