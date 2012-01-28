(ns gelfino.test.header
   (:use clojure.test gelfino.header) 
    )

(deftest chunked-header-assertions
    (is (thrown? java.lang.AssertionError (chunked-header '())))     
    (is ; total <= sequence
      (thrown? java.lang.AssertionError (chunked-header (concat (range 10) '(3 2))))) 
    (is ; total < 0 
      (thrown? java.lang.AssertionError (chunked-header (concat (range 10) '(3 -1)))))
    (is ; sequence < 0 
      (thrown? java.lang.AssertionError (chunked-header (concat (range 10) '(-1 1)))))
   )
