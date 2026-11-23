package nl.smartstreamlabs.sbwdroneconfig;
/** Names differ between 1.20 and 1.21; texture metadata is never a material name. */
final class WhiteHotMaterials {
    private WhiteHotMaterials(){}
    static boolean capture(String serialized){
        String n=serialized.toLowerCase(java.util.Locale.ROOT);
        if(n.startsWith("rendertype["))n=n.substring("rendertype[".length());
        int colon=n.indexOf(':');if(colon>=0)n=n.substring(0,colon);
        int bracket=n.indexOf('[');if(bracket>=0)n=n.substring(0,bracket);
        return !(n.contains("glint")||n.contains("shadow")||n.contains("outline")||n.contains("water_mask")||n.startsWith("text")||n.startsWith("debug")||n.startsWith("lines")||n.startsWith("line_strip"));
    }
}
