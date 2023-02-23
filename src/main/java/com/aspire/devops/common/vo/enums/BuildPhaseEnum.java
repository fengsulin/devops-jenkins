package com.aspire.devops.common.vo.enums;
//QUEUED,STARTED,FINALIZED

public enum BuildPhaseEnum {
    QUEUED("QUEUED"),
    STARTED("STARTED"),
    FINALIZED("FINALIZED");

    public String getPhase() {
        return phase;
    }

    private final String phase;

    BuildPhaseEnum(String phase) {
        this.phase = phase;
    }

    public String value(){
        return this.phase;
    }
}
