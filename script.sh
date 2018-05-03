./gradlew -q getCustomerHistory -PcustomerName=Carter -Prole=officer
./gradlew -q getCustomerHistory -PcustomerName=Carter -Prole=clerk
ls user-config/security/roles/
ls user-config/security/protected-paths/
ls user-config/security/query-rolesets/

vim plugins/entities/Customer/Customer.entity.json
./gradlew -q mlLoadModules 
./gradlew -q hubGeneratePii
./gradlew -q mlDeploySecurity
ls user-config/security/protected-paths/
ls user-config/security/query-rolesets/

vim user-config/security/roles/clerk.json
./gradlew -q mlDeploySecurity

./gradlew -q getCustomerHistory -PcustomerName=Carter -Prole=clerk
./gradlew -q getCustomerHistory -PcustomerName=Carter -Prole=officer
./gradlew getCustomerHistoryBySSN -Pssn=228-80-9858 -Prole=clerk
./gradlew getCustomerHistoryBySSN -Pssn=228-80-9858 -Prole=officer
