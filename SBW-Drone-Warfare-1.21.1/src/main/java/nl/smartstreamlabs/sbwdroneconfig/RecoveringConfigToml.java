package nl.smartstreamlabs.sbwdroneconfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import java.util.*;

/** Strict for valid TOML; salvage independent statements only after a syntax error. Originals are backed up by the caller. */
public final class RecoveringConfigToml {
 private static final java.util.regex.Pattern ASSIGNMENT=java.util.regex.Pattern.compile("(?:[A-Za-z0-9_.-]+|\"[^\"]+\"|'[^']+')\\s*=.*");
 public record Result(CommentedConfig config,List<String> notices){public Result{notices=List.copyOf(notices);}}
 private RecoveringConfigToml(){}
 public static Result parse(String input){
  try{return new Result(new TomlParser().parse(input),List.of());}
  catch(RuntimeException malformed){
   var notes=new ArrayList<String>();var accepted=new StringBuilder();recover(Arrays.asList(input.split("\\R",-1)),accepted,notes,1);
   return new Result(new TomlParser().parse(accepted.toString()),notes);
  }
 }
 private static void recover(List<String> lines,StringBuilder accepted,List<String> notes,int firstLine){
  while(true){
  var pending=new ArrayList<String>();var candidate=new StringBuilder();var state=new SyntaxState();int start=firstLine;
  for(int i=0;i<lines.size();i++){
   String line=lines.get(i);
   if(!pending.isEmpty()&&!state.triple&&beginsStatement(line)){
    notes.add("Skipped malformed configuration statement at lines "+start+"-"+(firstLine+i-1));pending.clear();candidate.setLength(0);state=new SyntaxState();
   }
   if(pending.isEmpty())start=firstLine+i;
   pending.add(line);candidate.append(line).append('\n');state.line(line);if(state.incomplete())continue;
   try{new TomlParser().parse(accepted.toString()+candidate);accepted.append(candidate);pending.clear();candidate.setLength(0);state=new SyntaxState();}
   catch(RuntimeException incomplete){}
  }
  if(!pending.isEmpty()){
   notes.add("Skipped incomplete configuration statement starting at line "+start);
   // An unterminated quoted field must not swallow all later independent settings.
   int restart=-1;for(int i=1;i<pending.size();i++)if(beginsStatement(pending.get(i))){restart=i;break;}
   if(restart>=0){lines=pending.subList(restart,pending.size());firstLine=start+restart;continue;}
  }
  return;
  }
 }
 private static boolean beginsStatement(String line){String s=line.stripLeading();return s.startsWith("[")||ASSIGNMENT.matcher(s).matches();}
 private static final class SyntaxState {
  int brackets,braces;char quote;boolean triple,escape;
  void line(String s){for(int i=0;i<s.length();i++){
   char c=s.charAt(i);
   if(quote!=0){if(escape){escape=false;continue;}if(c=='\\'&&quote=='"'){escape=true;continue;}if(c==quote){if(!triple){quote=0;}else if(i+2<s.length()&&s.charAt(i+1)==quote&&s.charAt(i+2)==quote){quote=0;triple=false;i+=2;}}continue;}
   if(c=='#')break;
   if(c=='"'||c=='\''){quote=c;triple=i+2<s.length()&&s.charAt(i+1)==c&&s.charAt(i+2)==c;if(triple)i+=2;}
   else if(c=='[')brackets++;else if(c==']')brackets--;else if(c=='{')braces++;else if(c=='}')braces--;
  }escape=false;if(!triple)quote=0;}
  boolean incomplete(){return triple||brackets>0||braces>0;}
 }
}
