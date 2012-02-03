package org.markdav.projectx.bof.numberservice.impl;

import java.util.UUID;

import org.markdav.projectx.bof.numberservice.api.INumberService;
import com.documentum.fc.client.DfService;
import com.documentum.fc.common.DfLogger;

/**
 * Default implementation of number service.
 * @author Mark Davidson
 */
public class NumberService extends DfService implements INumberService {

	/** The version of the service. */
	private static final String VERSION = "1.0";

	/** The vendor string. */
	private static final String COPYRIGHT = "Copyright 2011, Mark Davidson";

	/*
	 * (non-Javadoc)
	 * @see org.markdav.projectx.bof.numberservice.api.INumberService#getNumber()
	 */
	@Override
	public String getNumber() throws Exception {
		DfLogger.debug(this, "About to get number..", null, null);
		final String strNumber = UUID.randomUUID().toString();
		DfLogger.debug(this, "Got number: " + strNumber, null, null);
		return strNumber;
	}

	/*
     * (non-Javadoc)
     * @see com.documentum.fc.client.DfService#getVersion()
     */
	@Override
    public String getVersion() {
    	return VERSION;
    }
    
    /*
     * (non-Javadoc)
     * @see com.documentum.fc.client.DfService#getVendorString()
     */
    @Override
    public String getVendorString() {
    	return COPYRIGHT;
    }
    
    /*
     * (non-Javadoc)
     * @see com.documentum.fc.client.DfService#isCompatible(java.lang.String)
     */
    @Override
    public boolean isCompatible(String version) {
    	return (version.equalsIgnoreCase(VERSION));
    }
}
