package org.wildfly.extras.creaper.commands.patching;

import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadResourceOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PatchingOperations {
    private final Operations ops;
    private final Address patchingAddress;

    public PatchingOperations(OnlineManagementClient client) {
        this.ops = new Operations(client);
        this.patchingAddress = Address.coreService("patching");
    }

    /**
     * @return list of history entries of all patches that have been applied; never {@code null}
     */
    public List<PatchHistoryEntry> getHistory() throws IOException {
        List<ModelNode> asList = ops.invoke("show-history", patchingAddress).listValue();
        List<PatchHistoryEntry> entries = new ArrayList<PatchHistoryEntry>(asList.size());

        for (ModelNode modelNode : asList) {
            PatchHistoryEntry phe = new PatchHistoryEntry();
            phe.setPatchId(modelNode.get("patch-id").asString());
            phe.setType(modelNode.get("type").asString());
            phe.setAppliedAt(modelNode.get("applied-at").asString());
            entries.add(phe);
        }
        return entries;
    }

    /**
     * @param patchId ID of the patch to look for
     * @return history entry of the patch specified by the ID (null if not found)
     */
    public PatchHistoryEntry getHistoryEntry(String patchId) throws IOException {
        List<PatchHistoryEntry> patchingHistory = getHistory();

        PatchHistoryEntry foundPatchEntry = null;
        for (PatchHistoryEntry entry : patchingHistory) {
            if (entry.getPatchId().equals(patchId)) {
                foundPatchEntry = entry;
                break;
            }
        }
        return foundPatchEntry;
    }

    /**
     * @return information about current patch state as a {@code PatchInfo} object; never {@code null}
     */
    public PatchInfo getPatchInfo() throws IOException {
        ModelNodeResult modelNodeResult = ops.readResource(patchingAddress, ReadResourceOption.RECURSIVE,
                ReadResourceOption.INCLUDE_RUNTIME);
        modelNodeResult.assertDefinedValue();
        ModelNode result = modelNodeResult.value();

        PatchInfo patchInfo = new PatchInfo();

        patchInfo.setCumulativePatchId(result.get("cumulative-patch-id").asString());
        patchInfo.setVersion(result.get("version").asString());
        for (ModelNode patch : result.get("patches").asList()) {
            patchInfo.getPatches().add(patch.asString());
        }

        return patchInfo;
    }

    /**
     * @return cumulative patch ID or {@code null} if no patch is installed
     * @throws IOException when IO error occurs
     */
    public String getCumulativePatchId() throws IOException {
        return ops.readAttribute(patchingAddress, "cumulative-patch-id").stringValue(null);
    }

    /**
     * @return active server version (may differ to "version" command output until restart)
     * or {@code null} when the "version" is undefined
     * @throws IOException when IO error occurs
     */
    public String getCurrentServerVersion() throws IOException {
        return ops.readAttribute(patchingAddress, "version").stringValue(null);
    }

    /**
     * @return list of applied patches IDs, or an empty immutable list if no patch is installed
     * @throws IOException when IO error occurs
     */
    public List<String> getPatchesIds() throws IOException {
        return ops.readAttribute(patchingAddress, "patches").stringListValue(Collections.<String>emptyList());
    }

    /**
     * @return whether there is any patch applied
     * @throws IOException when IO error occurs
     */
    public boolean isAnyPatchInstalled() throws IOException {
        return !getHistory().isEmpty();
    }

    /**
     * @param patchId patch id to check
     * @return whether certain patch is installed
     * @throws IOException when IO error occurs
     */
    public boolean isPatchInstalled(String patchId) throws IOException {
        if (patchId == null) {
            throw new IllegalArgumentException("patchId is null");
        }

        if (patchId.equals(getCumulativePatchId())) {
            return true;
        }

        for (String id : getPatchesIds()) {
            if (patchId.equals(id)) {
                return true;
            }
        }
        PatchHistoryEntry phe = new PatchHistoryEntry(patchId);
        return getHistory().contains(phe);
    }

    public static final class PatchInfo {
        private String version; // 6.2.0.GA
        private String cumulativePatchId; // "base" == no patch
        private List<String> patches = new ArrayList<String>();

        public PatchInfo() {
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getCumulativePatchId() {
            return cumulativePatchId;
        }

        public void setCumulativePatchId(String cumulativePatchId) {
            this.cumulativePatchId = cumulativePatchId;
        }

        public List<String> getPatches() {
            return patches;
        }

        public void setPatches(List<String> patches) {
            this.patches = patches;
        }

        @Override
        public String toString() {
            return "PatchInfo{"
                    + "version='" + version + '\''
                    + ", cumulativePatchId='" + cumulativePatchId + '\''
                    + ", patches=" + patches
                    + '}';
        }
    }

    public static final class PatchHistoryEntry {
        private String patchId;
        private String type;
        private String appliedAt;

        public PatchHistoryEntry(String patchId, String type, String appliedAt) {
            this.patchId = patchId;
            this.type = type;
            this.appliedAt = appliedAt;
        }

        public PatchHistoryEntry(String patchId) {
            this.patchId = patchId;
        }

        public PatchHistoryEntry() {
        }

        public String getPatchId() {
            return patchId;
        }

        public void setPatchId(String patchId) {
            this.patchId = patchId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAppliedAt() {
            return appliedAt;
        }

        public void setAppliedAt(String appliedAt) {
            this.appliedAt = appliedAt;
        }

        @Override
        public boolean equals(Object other) {
            if (other != null && other instanceof PatchHistoryEntry) {
                PatchHistoryEntry that = (PatchHistoryEntry) other;
                return patchId.equals(that.patchId);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return patchId.hashCode();
        }

        @Override
        public String toString() {
            return "PatchHistoryEntry {patchId='" + patchId + "\', type='" + type + "\', appliedAt='" + appliedAt + "\'}";
        }
    }
}
