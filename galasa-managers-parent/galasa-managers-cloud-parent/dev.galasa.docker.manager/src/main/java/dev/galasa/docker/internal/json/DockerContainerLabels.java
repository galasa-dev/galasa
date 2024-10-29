/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.json;

public class DockerContainerLabels {
        private String EngineId;
        private String GALASA;
        private String RunId;
        private String SlotId;

        public void setEngineId(String EngineId) {
            this.EngineId = EngineId;
        }
        public String getEngineId() {
            return this.EngineId;
        }
        
        public void setGALASA(String GALASA) {
            this.GALASA = GALASA;
        }
        public String getGALASA() {
            return this.GALASA;
        }
       
        public void setRunId(String RunId) {
            this.RunId = RunId;
        }
        public String getRunId() {
            return this.RunId;
        }
       
        public void setSlotId(String SlotId) {
            this.SlotId = SlotId;
        }
        public String getSlotId() {
            return this.SlotId;
        }
    
}