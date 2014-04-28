/***************************************************************************
 *  Copyright (C) 2012 by H-Store Project                                  *
 *  Brown University                                                       *
 *  Massachusetts Institute of Technology                                  *
 *  Yale University                                                        *
 *                                                                         *
 *  Original By: VoltDB Inc.											   *
 *  Ported By:  Justin A. DeBrabant (http://www.cs.brown.edu/~debrabant/)  *								   
 *                                                                         *
 *                                                                         *
 *  Permission is hereby granted, free of charge, to any person obtaining  *
 *  a copy of this software and associated documentation files (the        *
 *  "Software"), to deal in the Software without restriction, including    *
 *  without limitation the rights to use, copy, modify, merge, publish,    *
 *  distribute, sublicense, and/or sell copies of the Software, and to     *
 *  permit persons to whom the Software is furnished to do so, subject to  *
 *  the following conditions:                                              *
 *                                                                         *
 *  The above copyright notice and this permission notice shall be         *
 *  included in all copies or substantial portions of the Software.        *
 *                                                                         *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,        *
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF     *
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. *
 *  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR      *
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,  *
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR  *
 *  OTHER DEALINGS IN THE SOFTWARE.                                        *
 ***************************************************************************/

package edu.brown.benchmark.voterdemohstoreanother;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.voltdb.VoltTable;
import org.voltdb.VoltTableRow;
import org.voltdb.client.Client;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.NoConnectionsException;
import org.voltdb.client.ProcedureCallback;

import weka.classifiers.meta.Vote;
import edu.brown.api.BenchmarkComponent;
import edu.brown.hstore.Hstoreservice.Status;
import edu.brown.logging.LoggerUtil.LoggerBoolean;

public class VoterDemoHStoreAnotherClient extends BenchmarkComponent {
    private static final Logger LOG = Logger.getLogger(VoterDemoHStoreAnotherClient.class);
    private static final LoggerBoolean debug = new LoggerBoolean();
    private static long lastTime;
    private static int timestamp;
    
    private String stat_filename;
    public static long count = 0l;
    public static long fixnum = 1000l;

    // Phone number generator
    //PhoneCallGenerator switchboard;
    
    // PhoneCall Generator
    edu.brown.stream.VoteGenerator switchboard;

    // Flags to tell the worker threads to stop or go
    AtomicBoolean warmupComplete = new AtomicBoolean(false);
    AtomicBoolean benchmarkComplete = new AtomicBoolean(false);

    // VoterDemoHStoreAnother benchmark state
    AtomicLong acceptedVotes = new AtomicLong(0);
    AtomicLong badContestantVotes = new AtomicLong(0);
    AtomicLong badVoteCountVotes = new AtomicLong(0);
    AtomicLong failedVotes = new AtomicLong(0);

    final Callback callback = new Callback();
    final AnotherCallback another_callback = new AnotherCallback();
    final StatisticCallback stat_callback =  new StatisticCallback();

    public static void main(String args[]) {
        BenchmarkComponent.main(VoterDemoHStoreAnotherClient.class, args, false);
    }

    public VoterDemoHStoreAnotherClient(String args[]) {
        super(args);
        int numContestants = VoterDemoHStoreAnotherUtil.getScaledNumContestants(this.getScaleFactor());
        //this.switchboard = new PhoneCallGenerator(this.getClientId(), numContestants);
        
        String timeLog = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        stat_filename = "voterdemohstoreanother-" + timeLog + ".txt";
        
        String filename = "voterdemohstoreanother-o-20000.ser";
        this.switchboard = new edu.brown.stream.VoteGenerator(filename);
        System.out.println( "filename: " + filename );
        System.out.println( "Size: " + switchboard.size() );
        
        lastTime = System.nanoTime();
        timestamp = 0;
    }

    @Override
    public void runLoop() {
        try {
            VoterDemoHStoreAnotherClient.count = 0l;
            
            System.out.println(String.valueOf(VoterDemoHStoreAnotherClient.count));
                    
            while (true) {
                // synchronously call the "Vote" procedure
                try {
                    runOnce();
                } catch (Exception e) {
                    failedVotes.incrementAndGet();
                }

            } // WHILE
        } catch (Exception e) {
            // Client has no clean mechanism for terminating with the DB.
            e.printStackTrace();
        }
    }

    @Override
    protected boolean runOnce() throws IOException {
        // Get the next phone call
    	if(System.nanoTime() - lastTime >= 1000000000)
        {
        	lastTime = System.nanoTime();
        	timestamp++;
        }
    	
        //PhoneCallGenerator.PhoneCall call = switchboard.receive();
    	edu.brown.stream.PhoneCallGenerator.PhoneCall call = switchboard.nextVote();
    	//timestamp = (int)call.voteId;
    	//call.debug();
    	
    	if(call != null)
    	{
            Client client = this.getClientHandle();
            boolean response = client.callProcedure(callback,
                                                    "Vote",
                                                    call.voteId,
                                                    call.phoneNumber,
                                                    call.contestantNumber,
                                                    VoterDemoHStoreAnotherConstants.MAX_VOTES,
                                                    timestamp);
            
            if(response)
            {
            	response = client.callProcedure(another_callback,
                        "GenerateLeaderboard");
            	
            	GetStatisticInfo();
            }
            return response;
    	}
    	else
    	    return true;
    }

    private void GetStatisticInfo()
    {
        try
        {
            VoterDemoHStoreAnotherClient.count++;
            if( VoterDemoHStoreAnotherClient.count == VoterDemoHStoreAnotherClient.fixnum )
            {
                //System.out.println("GetStatisticInfo() 1- " + String.valueOf(VoterDemoHStoreAnotherClient.fixnum));
                //System.out.println("call GetStatisticInfo ...");
                
                Client client = this.getClientHandle();
                client.callProcedure(stat_callback, "Results");

                VoterDemoHStoreAnotherClient.fixnum += 1000l;
                //System.out.println("GetStatisticInfo() 2- " + String.valueOf(VoterDemoHStoreAnotherClient.fixnum));
                
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String[] getTransactionDisplayNames() {
        // Return an array of transaction names
        String procNames[] = new String[]{
            Vote.class.getSimpleName()
        };
        return (procNames);
    }

    private class Callback implements ProcedureCallback {

        @Override
        public void clientCallback(ClientResponse clientResponse) {
            // Increment the BenchmarkComponent's internal counter on the
            // number of transactions that have been completed
            incrementTransactionCounter(clientResponse, 0);
            
            // Keep track of state (optional)
            if (clientResponse.getStatus() == Status.OK) {
                VoltTable results[] = clientResponse.getResults();
                assert(results.length == 1);
                long status = results[0].asScalarLong();
                if (status == VoterDemoHStoreAnotherConstants.VOTE_SUCCESSFUL) {
                    acceptedVotes.incrementAndGet();
                }
                else if (status == VoterDemoHStoreAnotherConstants.ERR_INVALID_CONTESTANT) {
                    badContestantVotes.incrementAndGet();
                }
                else if (status == VoterDemoHStoreAnotherConstants.ERR_VOTER_OVER_VOTE_LIMIT) {
                    badVoteCountVotes.incrementAndGet();
                }
            }
            else if (clientResponse.getStatus() == Status.ABORT_UNEXPECTED) {
                if (clientResponse.getException() != null) {
                    clientResponse.getException().printStackTrace();
                }
                if (debug.val && clientResponse.getStatusString() != null) {
                    LOG.warn(clientResponse.getStatusString());
                }
            }
            
        }
        
    } // END CLASS
    
    private class AnotherCallback implements ProcedureCallback {

        @Override
        public void clientCallback(ClientResponse clientResponse) {
            // Increment the BenchmarkComponent's internal counter on the
            // number of transactions that have been completed
            incrementTransactionCounter(clientResponse, 0);
        }
    } // END CLASS
    
    private class StatisticCallback implements ProcedureCallback {
        @Override
        public void clientCallback(ClientResponse clientResponse)
        {
            if (clientResponse.getStatus() == Status.OK) {
                VoltTable vt = clientResponse.getResults()[0];
                
                int row_len = vt.getRowCount();
                
                String line =  String.valueOf(VoterDemoHStoreAnotherClient.fixnum - 1000l) + ": ";
                for(int i=0;i<row_len; i++)
                {
                    VoltTableRow row = vt.fetchRow(i);
                    String contestant_name = row.getString(0);
                    long total_votes = row.getLong(2);
                    
                    String content = contestant_name + "-" + String.valueOf(total_votes);
                    
                    line += content + " ";
                }
                
                //System.out.println(line);
                
                try {
                    WriteToFile(line);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }            
        }
        
        private void WriteToFile(String content) throws IOException
        {
            //System.out.println(stat_filename + " : " + content );
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(stat_filename, true)));
            out.println(content);
            out.close();
        }
    }
    
}
