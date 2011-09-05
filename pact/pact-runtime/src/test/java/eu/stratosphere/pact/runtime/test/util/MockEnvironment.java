/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.pact.runtime.test.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.execution.Environment;
import eu.stratosphere.nephele.io.DefaultRecordDeserializer;
import eu.stratosphere.nephele.io.GateID;
import eu.stratosphere.nephele.io.InputGate;
import eu.stratosphere.nephele.io.OutputGate;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.services.iomanager.IOManager;
import eu.stratosphere.nephele.services.memorymanager.MemoryManager;
import eu.stratosphere.nephele.services.memorymanager.spi.DefaultMemoryManager;
import eu.stratosphere.nephele.types.Record;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.runtime.util.MutableObjectIterator;


public class MockEnvironment extends Environment
{
	private MemoryManager memManager;

	private IOManager ioManager;

	private Configuration config;

	private List<InputGate<PactRecord>> inputs;
	private List<OutputGate<PactRecord>> outputs;
	
	private final JobID jobId = new JobID();

	public MockEnvironment(long memorySize) {
		this.config = new Configuration();
		this.inputs = new ArrayList<InputGate<PactRecord>>();
		this.outputs = new ArrayList<OutputGate<PactRecord>>();
	
		this.memManager = new DefaultMemoryManager(memorySize);
		this.ioManager = new IOManager(System.getProperty("java.io.tmpdir"));
	}

	public void addInput(MutableObjectIterator<PactRecord> inputIterator) {
		int id = inputs.size();
		inputs.add(new MockInputGate(id, inputIterator));
	}
	
	public void addOutput(List<PactRecord> outputList) {
		int id = outputs.size();
		outputs.add(new MockOutputGate(id, outputList));
	}

	@Override
	public Configuration getRuntimeConfiguration() {
		return this.config;
	}

	@Override
	public boolean hasUnboundInputGates() {
		return this.inputs.size() > 0 ? true : false;
	}

	@Override
	public boolean hasUnboundOutputGates() {
		return this.outputs.size() > 0 ? true : false;
	}

	@Override
	public InputGate<? extends Record> getUnboundInputGate(int gateID) {
		return inputs.remove(gateID);
	}

	@Override
	public eu.stratosphere.nephele.io.OutputGate<? extends Record> getUnboundOutputGate(int gateID) {
		return outputs.remove(gateID);
	}

	@Override
	public MemoryManager getMemoryManager() {
		return this.memManager;
	}

	@Override
	public IOManager getIOManager() {
		return this.ioManager;
	}
	
	public JobID getJobID() {
		return this.jobId;
	}

	private static class MockInputGate extends InputGate<PactRecord> {

		private MutableObjectIterator<PactRecord> it;

		public MockInputGate(int id, MutableObjectIterator<PactRecord> it) {
			super(new JobID(), new GateID(), new DefaultRecordDeserializer<PactRecord>(PactRecord.class), id, null);
			this.it = it;
		}

		@Override
		public PactRecord readRecord(PactRecord target) throws IOException, InterruptedException {
			
			if (it.next(target)) {
				return target;
			} else {
				return null;
			}
		}
	}
	
	private static class MockOutputGate extends OutputGate<PactRecord> {
		
		private List<PactRecord> out;
		
		public MockOutputGate(int index, List<PactRecord> outList) {
			super(new JobID(), new GateID(), PactRecord.class, index, null ,false);
			this.out = outList;
		}

		@Override
		public void writeRecord(PactRecord record) throws IOException, InterruptedException {
			out.add(record.createCopy());
		}

	}

}
