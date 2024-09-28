package data.util;

/**
 * Enum signifying level to log with, will determine whether the message will be logged by using nothing,
 * {@link org.apache.log4j.Logger#debug(Object)}, {@link org.apache.log4j.Logger#info(Object)}, 
 * {@link org.apache.log4j.Logger#warn(Object)}, {@link org.apache.log4j.Logger#error(Object)} or 
 * {@link org.apache.log4j.Logger#fatal(Object)}
 */
public enum LoggerLogLevel { NONE, DEBUG, INFO, WARN, ERROR, FATAL }