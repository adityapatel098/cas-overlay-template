package org.apereo.cas.config;

import org.apereo.cas.acct.AccountRegistrationPropertyLoader;
import org.apereo.cas.acct.AccountRegistrationResponse;
import org.apereo.cas.acct.DefaultAccountRegistrationPropertyLoader;
import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.apereo.cas.configuration.CasConfigurationProperties;

@AutoConfiguration
//@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOverlayOverrideConfiguration {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

    /*
    @Bean
    public MyCustomBean myCustomBean() {
        ...
    }
     */

	@Bean
	public AccountRegistrationPropertyLoader accountMgmtRegistrationPropertyLoader() {
	    return new DefaultAccountRegistrationPropertyLoader(null);
	}
	
	@Bean
	public AccountRegistrationProvisionerConfigurer customProvisioningConfigurer() {
	    return () -> {
	        return (request) ->{
	        	try {
	        		StringBuilder query = new StringBuilder();
	        	    query.append(" INSERT INTO auth_user (first_name, last_name, email, password)");
		        	query.append(" VALUES (?, ?, ?, ?)");

		        	jdbcTemplate.execute(query.toString(), new PreparedStatementCallback<Boolean>(){
		        	    @Override
		        	    public Boolean doInPreparedStatement(PreparedStatement ps)
		        	            throws SQLException, DataAccessException {

			        	        ps.setString(1,request.getFirstName());
			        	        ps.setString(2,request.getLastName());
			        	        ps.setString(3,request.getEmail());
			        	        ps.setString(3,request.getPassword());

			        	        return ps.execute();

		        	    	}
		        	    });
		        	return AccountRegistrationResponse.success();
	        	} catch(Exception e) {
	        		return AccountRegistrationResponse.failure();
	        	}

	        };
	    };
	}
}
