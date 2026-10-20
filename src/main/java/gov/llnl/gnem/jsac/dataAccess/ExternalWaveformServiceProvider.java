/*-
 * #%L
 * Java Seismic Analysis Code (JSAC)
 *  LLNL-CODE-855505
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2022 - 2026 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.jsac.dataAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import llnl.gnem.core.dataObjects.ExternalWaveformService;

public class ExternalWaveformServiceProvider {
    
    private final Map<String, ExternalWaveformService> waveformServiceMap;
    private ExternalWaveformService currentService;
    
    private ExternalWaveformServiceProvider() {
        waveformServiceMap = new HashMap<>();
        currentService = null;
        ServiceLoader<ExternalWaveformService> list = ServiceLoader.load(ExternalWaveformService.class);
        for (ExternalWaveformService tmp : list) {
            waveformServiceMap.put(tmp.getName(), tmp);
            if (tmp.getName().equalsIgnoreCase("FDSN")) {
                currentService = tmp;
            }
        }
        if(currentService == null){
            throw new IllegalStateException("Unable to load FDSN waveform service!");
        }
    }
    
    public Collection<String> getServiceNames() {
        return waveformServiceMap.keySet();
    }
    
    public ExternalWaveformService getCurrentService() {
        return currentService;
    }
    
    public void setCurrentService(String serviceName) {
        ExternalWaveformService service = waveformServiceMap.get(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("Invalid or unsupported waveform service name!");
        } else {
            currentService = service;
        }
    }
    
    public Collection<ExternalWaveformService> getAllServices() {
        Collection<ExternalWaveformService> result = waveformServiceMap.values();
        return result != null ? result : new ArrayList<>();
    }
    
    public static ExternalWaveformServiceProvider getInstance() {
        return ExternalWaveformServiceProviderHolder.INSTANCE;
    }
    
    public Collection<String> getServiceDescriptions() {
        Collection<String> result = new ArrayList<>();
        for (ExternalWaveformService svc : waveformServiceMap.values()) {
            result.add(svc.toString());
        }
        return result;
    }
    
    private static class ExternalWaveformServiceProviderHolder {
        
        private static final ExternalWaveformServiceProvider INSTANCE = new ExternalWaveformServiceProvider();
    }
}
