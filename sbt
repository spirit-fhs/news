java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512m -Xmx1512M -jar `dirname $0`/sbt-launch-0.7.7.jar "$@"
