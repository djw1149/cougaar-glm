Database=jdbc:oracle:thin:@${org.cougaar.database}
Driver = oracle.jdbc.driver.OracleDriver
Username = ${blackjack8.database.user}
Username = ${blackjack8.database.password}
MIN_IN_POOL= 1
MAX_IN_POOL= 4
TIMEOUT= 1
NUMBER_OF_TRIES= 2

losQuery=select * from LENGTH_OF_STAY 
