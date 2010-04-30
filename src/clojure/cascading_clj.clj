(ns cascading-clj
  (:require [cascading-clj.imports :as imp])
  (:import (java.util Properties))
  (:use (cascading-clj io tuples)))

(imp/import-all)

(defmacro gen-flow []
  "Generates a class in the calling namespace that proxies a main[] method. The method's name should be tool-main. The assemble* family
  of functions generate tool-main when called."
  (let [the-name (.replace (str (ns-name *ns*)) \- \_)]
    `(gen-class
      :name ~the-name
      :prefix "tool-"
      :main true)))

(defn assemble*
  "Given a pipe description, a bag of java.util.Properties, a function yielding a Source from a given input path,
  one yielding a sink from a given input path, a sequence of chained assembly operations, and a function taking a connected Flow,
  create a pipeline from the source sink, through the chain, to the target sink, and apply flow-fn to it."
  ([desc props sourcefn sinkfn pipes flowfn] 
   (let [the-name (.replace (str (ns-name *ns*)) \- \_)]
     (intern *ns* 'tool-main
             (fn [& args]
               (FlowConnector/setApplicationJarClass props (Class/forName the-name))
               (let [flow (FlowConnector. props)
                     pipe (Pipe. desc)
                     chained (pipes pipe)]
                 (flowfn (.connect flow desc (sourcefn (first args)) (sinkfn (last args)) chained)))))))
  ([desc sourcefn sinkfn pipes flowfn] (assemble* desc (Properties.) sourcefn sinkfn flowfn pipes)))

(defn assemble-props
  "Adds a standard main method, named tool-main, to the current
  namespace, that will execute the flow-fn on the given flow when run."
  ([desc outputs props pipes flow-fn]
   (let [source #(hdfs-source %)
         sink #(hdfs-delimited-sink % (fields outputs))
         jobconf (doto (Properties.) (.putAll props))]
     (assemble* desc jobconf source sink flow-fn pipes)))
  ([desc outputs props pipes] 
   (assemble-props desc outputs props pipes (memfn complete))))

(defn assemble
  "Adds a standard main method, named tool-main, to the current
  namespace, that will execute the flow-fn on the given flow when run."
  ([desc outputs pipes flow-fn]
   (assemble-props desc outputs {} pipes flow-fn))
   ([desc outputs pipes] (assemble desc outputs pipes (memfn complete))))
