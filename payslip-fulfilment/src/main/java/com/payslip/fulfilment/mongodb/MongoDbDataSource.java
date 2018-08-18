package com.payslip.fulfilment.mongodb;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MongoDbDataSource {

    private MongoDatabase mongoDb;
    private static final Logger logger = Logger.getLogger(MongoDbDataSource.class.getName());

    @Inject
    @ConfigProperty(name = "mongodb.url")
    String dbURIString;

    @Inject
    @ConfigProperty(name = "mongodb.db")
    String db;

    @PostConstruct
    private void initProperties() {
        logger.log(Level.INFO, "--- Initializing Datasource ---\nDB_URL={0}, DB={1}", new Object[]{dbURIString, db});

        MongoClient mongoClient = new MongoClient(new MongoClientURI(dbURIString));
        mongoDb = mongoClient.getDatabase(db);

        logger.log(Level.INFO, "Datasource initialized");
    }

    @Produces
    @RequestScoped
    public MongoDatabase exposeMongoDbSource() throws IOException {
        return mongoDb;
    }

}
