package com.xkball.xorlib.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

public class FileClassSearcher {
    
    public static String[] CLASS_PATH;
    public static final Map<String,ClassResourcePath> classFiles = new HashMap<>();
    
    static {
        CLASS_PATH = System.getProperty("java.class.path").split(File.pathSeparator);
        init();
    }
    
    public static String fileToClassName(String fileName) {
        return fileName.replace('/','.').replace('\\','.').substring(0,fileName.length()-6);
    }
    
    public static void init(){
        var temp = new HashMap<String, ClassResourcePath>();
        for(var rootStr : CLASS_PATH){
            var root = Path.of(rootStr);
            if(Files.isRegularFile(root) && rootStr.endsWith(".jar")){
                try(var jar = new JarFile(rootStr)){
                    jar.stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> temp.put(fileToClassName(entry.getName()),new ClassResourcePath(root,Path.of(entry.getName()))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(Files.isDirectory(root)){
                try {
                    //noinspection resource
                    var paths = Files.find(root,Integer.MAX_VALUE,(p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class")).toList();
                    for(var path : paths){
                        var relativePath = root.relativize(path);
                        temp.put(fileToClassName(relativePath.toString()),new ClassResourcePath(root,relativePath));
                    }
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        }
        classFiles.clear();
        classFiles.putAll(temp);
    }
    
    public record ClassResourcePath(Path root, Path path){
        
        public Optional<ByteCode> read(){
            if(Files.isDirectory(root)){
                try {
                    return Optional.of(new ByteCode(fileToClassName(path.toString()),Files.readAllBytes(root.resolve(path))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(Files.isRegularFile(root) && root.toString().endsWith(".jar")){
                try(var jar = new JarFile(root.toFile())){
                    var entry = jar.getJarEntry(path.toString());
                    if(entry == null) return Optional.empty();
                    return Optional.of(new ByteCode(fileToClassName(path.toString()),jar.getInputStream(entry).readAllBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return Optional.empty();
        }
    }
}
