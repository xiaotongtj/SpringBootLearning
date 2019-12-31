package com.forezp.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            //copy 源 + 目标（rootLocation/filename）
            Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            System.out.println("---------loadAll before---------");
            Files.walk(this.rootLocation, 1).forEach(System.out::println);
            Stream<Path> pathStream = Files.walk(this.rootLocation, 1)//获取rootLocaltion中路径下的所有path,排除了根目录
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);

            System.out.println("---------loadAll after---------");
            //pathStream.forEach(System.out::println);

            return pathStream;
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    //拼接路径
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    //
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    public static void main(String[] args) {
        //目录和目录下文件的相对路径就是文件名称
        Path path = Paths.get("D:/hello");
        Path path1 = Paths.get("D:/hello/hello.txt");
        Path relativize = path.relativize(path1);
        System.out.println(relativize);
    }
}
