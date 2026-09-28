mvn dependency:copy-dependencies \
  -DoutputDirectory=target/dependencies \
  -DincludeScope=runtime \
  -DexcludeScope=provided