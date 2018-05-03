./gradlew getCustomerHistory -PcustomerName=Carter -Prole=officer
./gradlew getCustomerHistory -PcustomerName=Carter -Prole=clerk
ls user-config/security/roles/
ls user-config/security/protected-paths/
ls user-config/security/query-rolesets/

vim user-config/security/roles/clerk.json
vim plugins/entities/Customer/Customer.entity.json
./gradlew mlLoadModules -i
./gradlew hubGeneratePii

./gradlew mlDeploySecurity
ls user-config/security/protected-paths/
ls user-config/security/query-rolesets/

./gradlew getCustomerHistory -PcustomerName=Carter -Prole=clerk
./gradlew getCustomerHistory -PcustomerName=Carter -Prole=officer
