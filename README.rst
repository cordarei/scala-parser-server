=============================================================
scala-parser-server: An HTTP interface to the Stanford Parser
=============================================================

This is a rough implementation of an HTTP interface to the Stanford
parser, to provide access to the parser from languages other than Java
more easily. This server allows multiple parses to proceed concurrently,
to facilitate parallelism when processing large amounts of text. The
maximum number of concurrent parses is limited to prevent overuse of
memory.
