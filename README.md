# binio

A Clojure library for reading and writing binary files.

## Usage

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.nighcoder/binio.svg)](https://clojars.org/org.clojars.nighcoder/binio)

Add the dependency [nighcoder/binio "0.1.1"] to your project.

Require the binio.core namespace.

`(require '[binio.core :as bio])`

The core namespace contains two functions to write to and read from files.

Spit-bytes writes a byte-array to file.

`(bio/spit-bytes "testfile" (byte-array [104 101 108 108 111 32 119 111 114 108 100 10]))`

Slurp-bytes reads a file and copies it's contents to a byte-array.

`(bio/slurp-bytes "testfile")`

## License

Copyright © 2020 Daniel Ciumberică

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **AS IS**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
