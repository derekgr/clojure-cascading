(ns cascading-clj.io
  (:require [cascading-clj.imports :as imp])
  (:use [cascading-clj.tuples]))

(imp/import-all)

(defn line-reader [field]
  (TextLine. (fields field)))

(defn text-writer [delimiter names]
  (TextDelimited. #^Fields names (str delimiter)))

(defn hdfs-source 
  "Returns a new Tap from the HDFS path given."
  ([reader path] (Hfs. #^Scheme reader #^String path))
  ([path] (hdfs-source (line-reader "line") path)))

(defn hdfs-sink 
  ([writer path] (Hfs. writer path SinkMode/REPLACE))
  ([path] (hdfs-sink (TextLine.) path)))

(defn hdfs-delimited-sink [path cols]
  "Produces a SinkTap that will write the tuplestream's named columns to the HDFS path given as tab-delimited text output."
  (hdfs-sink (text-writer "\t" cols) path))
