===================
scala-parser-server
===================
----------------------------------------
An HTTP interface to the Stanford Parser
----------------------------------------


Overview
========

This is a rough implementation of an HTTP interface to the Stanford
parser, to provide access to the parser from languages other than Java
more easily. This server allows multiple parses to proceed concurrently,
to facilitate parallelism when processing large amounts of text. The
maximum number of concurrent parses is limited to prevent overuse of
memory.


License
=======

Copyright 2013 Joseph Irwin.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
