workspace {

    model {
            root_producer = softwareSystem "Producer" "Produces data for the system" "Producer"
            ftpServer = softwareSystem "FTP Server" "Provides the data through FTP" "Server"
            storageSystem = softwareSystem "Progress Storage" "Stores the already processed data" "Database"
            kafkaPipeRawData = softwareSystem "Kafka Pipe" "Ingests and processes raw data using Kafka" "Kafka"
            RawConsumerAndCustomTopicProducer = softwareSystem "Consumer / Producer" "Consumes raw data and produces customized information" "Producer"
            kafkaTopicYearTemp = softwareSystem "Year Temp Topic" "Collects topic information related to temperature data" "Kafka"
            kafkaTopicOther = softwareSystem "Other Topic" "Collects topic information for custom data" "Kafka"
            yearTempTopicStreamProcessing = softwareSystem "Stream Processing System for Year Temp Topic" "Processes data related to temperature topic" "Producer"
            otherTopicStreamProcessing = softwareSystem "Stream Processing System for Other Topic" "Processes data related to the custom topic" "Producer"
    
            root_producer -> ftpServer "Accesses Files"
            root_producer -> storageSystem "Stores Progress Data"
            root_producer -> kafkaPipeRawData "Publishes Raw Data"
            kafkaPipeRawData -> RawConsumerAndCustomTopicProducer "Consumes Raw Data"
            RawConsumerAndCustomTopicProducer -> kafkaTopicYearTemp "Publishes Customized Information for Year Temp Topic"
            RawConsumerAndCustomTopicProducer -> kafkaTopicOther "Publishes Customized Information for Other Topic"
            kafkaTopicYearTemp -> yearTempTopicStreamProcessing "Consumes Year Temp Data"
            kafkaTopicOther -> otherTopicStreamProcessing "Consumes Other Specific Data"
        }

    views {
     systemContext root_producer "Root_Producer" {
            include *
            autoLayout
            title "Root Producer System Context"
            description "The root producer system and its interactions with other systems."
        }
    
        systemContext kafkaPipeRawData "RawDatasourceKafkaPipe" {
            include *
            autoLayout
            title "Raw Datasource Kafka Pipe System Context"
            description "The Kafka pipe for raw data ingestion and publishing."
        }
    
        systemContext RawConsumerAndCustomTopicProducer "RawConsumerAndCustomTopicProducer" {
            include *
            autoLayout
            title "Raw Consumer / Custom Topic Producer System Context"
            description "The system responsible for consuming raw data and producing customized information."
        }
    
        systemContext kafkaTopicYearTemp "KafkaSpecificTopicYearTemp" {
            include *
            autoLayout
            title "Kafka Specific Topic (Year Temp) System Context"
            description "The system responsible for collecting topic information for temperature data."
        }
    
        systemContext kafkaTopicOther "KafkaSpecificTopicOther" {
            include *
            autoLayout
            title "Kafka Specific Topic (Other) System Context"
            description "The system responsible for collecting topic information for custom data."
        }
    
        systemLandscape "Landscape" {
            include *
            autoLayout
            title "System Landscape"
            description "The overall landscape of the system with all components and their interactions."
        }
    
        styles {
            element "User" {
                color #ffffff
                fontSize 22
                shape Person
            }
            element "Database" {
                color #ffffff
                fontSize 22
                shape Cylinder
                background #1168bd
            }
            element "Kafka" {
                color #ffffff
                fontSize 22
                shape Pipe
                background #438dd5
            }
            element "Producer" {
                color #000000
                fontSize 22
                shape Box
                background #91F179
            }
            element "Server" {
                color #ffffff
                fontSize 22
                shape Folder
                background #EACE6D
            }
            element "Customer" {
                background #ff0000
            }
        }
     }
}