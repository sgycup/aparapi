/**
 * Copyright (c) 2016 - 2018 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aparapi.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.JavaDevice;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.kernel.KernelManager;

/**
 * Base tests for validation of the correctness of the atomics function computations, both on Java and on OpenCL.
 * @author CodeRasurae
 */
public class AtomicsSupportTest {

    private static OpenCLDevice openCLDevice = null;

    private class CLKernelManager extends KernelManager {
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.ACC, Device.TYPE.GPU, Device.TYPE.CPU);
    	}
    }
    
    private class JTPKernelManager extends KernelManager {
    	private JTPKernelManager() {
    		LinkedHashSet<Device> preferredDevices = new LinkedHashSet<Device>(1);
    		preferredDevices.add(JavaDevice.THREAD_POOL);
    		setDefaultPreferredDevices(preferredDevices);
    	}
    	@Override
    	protected List<Device.TYPE> getPreferredDeviceTypes() {
    		return Arrays.asList(Device.TYPE.JTP);
    	}
    }
    
    @After
    public void classTeardown() {
    	Util.resetKernelManager();
    }
    
    @Before
    public void setUpBeforeClass() throws Exception {
    	KernelManager.setKernelManager(new CLKernelManager());
        Device device = KernelManager.instance().bestDevice();
        assumeTrue (device != null && device instanceof OpenCLDevice);
        openCLDevice = (OpenCLDevice) device;
    }
    
    @Test
    public void testAtomicAddOpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicAdd kernel = new AtomicAdd(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + in[1], out[1]);
    }

    @Test
    public void testAtomicAddOpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicAdd kernel = new AtomicAdd(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + in[1], out[1]);
    }

    @Test
    public void testAtomicAddJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicAdd kernel = new AtomicAdd(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + in[1], out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicAdd.
     * Validates that a add operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicAdd extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];
    	
    	public AtomicAdd(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicAdd(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}
    }

    @Test
    public void testAtomicSubOpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicSub kernel = new AtomicSub(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - in[1], out[1]);
    }

    @Test
    public void testAtomicSubOpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicSub kernel = new AtomicSub(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - in[1], out[1]);
    }

    @Test
    public void testAtomicSubJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicSub kernel = new AtomicSub(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - in[1], out[1]);
    }
    
    /**
     * Kernel for single threaded validation of atomicSub.
     * Validates that a subtraction operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicSub extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicSub(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicSub(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);			
		}

    }

    @Test
    public void testAtomicXchgOpenCLExplicit() {
    	
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicXchg kernel = new AtomicXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicXchgOpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicXchg kernel = new AtomicXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }
    
    @Test
    public void testAtomicXchgJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 10;
    	in[1] = 20;
    	
    	final AtomicXchg kernel = new AtomicXchg(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicXchg.
     * Validates that a value exchange operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicXchg extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicXchg(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicXchg(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }
    
    @Test
    public void testAtomicIncOpenCLExplicit() {
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicInc kernel = new AtomicInc(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + 1, out[1]);
    }

    @Test
    public void testAtomicIncOpenCL() {
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicInc kernel = new AtomicInc(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + 1, out[1]);
    }
    
    @Test
    public void testAtomicInc() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicInc kernel = new AtomicInc(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] + 1, out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicInc.
     * Validates that an increment operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicInc extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];
    	
    	public AtomicInc(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicInc(atomicValues[0]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicDecOpenCLExplicit() {
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicDec kernel = new AtomicDec(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - 1, out[1]);
    }

    @Test
    public void testAtomicDecOpenCL() {
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicDec kernel = new AtomicDec(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - 1, out[1]);
    }

    @Test
    public void testAtomicDecJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[1];
    	final int[] out = new int[2];
    	in[0] = 50;
    	
    	final AtomicDec kernel = new AtomicDec(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0] - 1, out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicDec.
     * Validates that a decrement operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicDec extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicDec(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicDec(atomicValues[0]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicCmpXchg1OpenCLExplicit() {
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 50;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[2], out[1]);
    }

    @Test
    public void testAtomicCmpXchg1OpenCL() {
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 50;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[2], out[1]);
    }

    @Test
    public void testAtomicCmpXchg1JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 50;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[2], out[1]);
    }

    @Test
    public void testAtomicCmpXchg2OpenCLExplicit() {
    	
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicCmpXchg2OpenCL() {
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicCmpXchg2JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[3];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	in[2] = 100;
    	
    	final AtomicCmpXchg kernel = new AtomicCmpXchg(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicCmpXchg.
     * Validates that a cmpXchg operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicCmpXchg extends Kernel {
    	private int in[];
    	private int out[];
    	
    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicCmpXchg(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicCmpXchg(atomicValues[0], in[1], in[2]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicMin1OpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicMin1OpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicMin1JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicMin2OpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicMin2OpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicMin2JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMin kernel = new AtomicMin(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicMin.
     * Validates that a min operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicMin extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicMin(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicMin(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);			
		}

    }

    @Test
    public void testAtomicMax1OpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicMax1OpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }
    
    @Test
    public void testAtomicMax1JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 51;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[1], out[1]);
    }

    @Test
    public void testAtomicMax2OpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicMax2OpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    @Test
    public void testAtomicMax2JTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 50;
    	in[1] = 49;
    	
    	final AtomicMax kernel = new AtomicMax(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", in[0], out[1]);
    }

    /**
     * Kernel for single threaded validation of atomicMax.
     * Validates that a max operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicMax extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicMax(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicMax(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicAndOpenCLExplicit() {
    	
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicAnd kernel = new AtomicAnd(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x81, out[1]);
    }

    @Test
    public void testAtomicAndOpenCL() {
    	
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicAnd kernel = new AtomicAnd(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x81, out[1]);
    }
    
    @Test
    public void testAtomicAndJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicAnd kernel = new AtomicAnd(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x81, out[1]);
    }
    
    /**
     * Kernel for single threaded validation of atomicXor.
     * Validates that an and operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicAnd extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicAnd(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicAnd(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicOrOpenCLExplicit() {
    	
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0x80;
    	in[1] = 0x02;
    	
    	final AtomicOr kernel = new AtomicOr(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x82, out[1]);
    }

    @Test
    public void testAtomicOrOpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0x80;
    	in[1] = 0x02;
    	
    	final AtomicOr kernel = new AtomicOr(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x82, out[1]);
    }

    @Test
    public void testAtomicOrJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0x80;
    	in[1] = 0x02;
    	
    	final AtomicOr kernel = new AtomicOr(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x82, out[1]);
    }
    
    /**
     * Kernel for single threaded validation of atomicOr.
     * Validates that an or operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicOr extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicOr(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicOr(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}

    }

    @Test
    public void testAtomicXorOpenCLExplicit() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicXor kernel = new AtomicXor(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.setExplicit(true);
	        kernel.put(in);
	        kernel.execute(range);
	        kernel.get(out);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x7e, out[1]);
    }

    @Test
    public void testAtomicXorOpenCL() {
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicXor kernel = new AtomicXor(in, out);
    	try {
	    	final Range range = openCLDevice.createRange(1,1);
	        kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x7e, out[1]);
    }
    
    @Test
    public void testAtomicXorJTP() {
    	KernelManager.setKernelManager(new JTPKernelManager());
    	Device device = KernelManager.instance().bestDevice();
    	final int in[] = new int[2];
    	final int[] out = new int[2];
    	in[0] = 0xf1;
    	in[1] = 0x8f;
    	
    	final AtomicXor kernel = new AtomicXor(in, out);
    	try {
	    	final Range range = device.createRange(1,1);
	    	kernel.execute(range);
    	} finally {
    		kernel.dispose();
    	}
    	assertEquals("Old value doesn't match", in[0], out[0]);
    	assertEquals("Final value doesn't match", 0x7e, out[1]);
    }
    
    /**
     * Kernel for single threaded validation of atomicXor.
     * Validates that a xor operation is actually performed.
     * @author lpnm
     *
     */
    private static final class AtomicXor extends Kernel {
    	private int in[];
    	private int out[];

    	@Local
    	private AtomicInteger atomicValues[];

    	public AtomicXor(int[] in, int out[]) {
    		this.in = in;
    		this.out = out;
    		atomicValues = new AtomicInteger[2];
    		atomicValues[0] = new AtomicInteger(0);
    		atomicValues[1] = new AtomicInteger(0);
    	}
    	
		@Override
		public void run() {
			atomicSet(atomicValues[0], in[0]);
			out[0] = atomicXor(atomicValues[0], in[1]);
			out[1] = atomicGet(atomicValues[0]);
		}
    }    
}
