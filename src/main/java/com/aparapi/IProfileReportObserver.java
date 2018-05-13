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
package com.aparapi;

import java.lang.ref.WeakReference;

import com.aparapi.device.Device;

/**
 * Defines interface for listener/observer of Kernel profile reports 
 * 
 * @author lpnm
 */
public interface IProfileReportObserver {
	
	/**
	 * The listener method will be invoked each time a profile report becomes available for each Aparapi Kernel which has
	 * a registered observer.<br/>
	 * <b>Note1: </b>A report will be generated by a thread executing a kernel. If multiple threads execute the same kernel,
	 * concurrently, this method can be called concurrently too, thus classes implementing this interface need to provide
	 * a thread safe method.
	 * <br/>
	 * <b>Note2: </b>If profiling information is to be stored has a {@link com.aparapi.ProfileReport}, it is necessary to clone 
	 * the profileInfo object with {@link com.aparapi.ProfileReport#clone()}. A WeakReference is used to help differentiate such
	 * need, however it is guaranteed that profileInfo will not be null, during the method execution.
	 * <br/>
	 * @param kernelClass the class of the kernel to which the profile report pertains
	 * @param device the device on which the kernel ran, producing the profile report
	 * @param profileInfo the profile report for the given Aparapi kernel and device pair
	 */
	public void receiveReport(final Class<? extends Kernel> kernelClass, final Device device, final WeakReference<ProfileReport> profileInfo);

}