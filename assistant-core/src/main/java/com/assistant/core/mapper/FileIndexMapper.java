package com.assistant.core.mapper;

import com.assistant.core.entity.FileIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件索引Mapper
 */
@Mapper
public interface FileIndexMapper extends BaseMapper<FileIndex> {
    
    /**
     * 根据文件路径查询
     */
    @Select("SELECT id, file_path, file_name, file_size, file_type, last_modified, indexed_time, folder_id, content, summary FROM file_index WHERE file_path = #{filePath}")
    FileIndex selectByFilePath(@Param("filePath") String filePath);
    
    /**
     * 根据文件夹ID查询文件列表
     */
    @Select("SELECT id, file_path, file_name, file_size, file_type, last_modified, indexed_time, folder_id, content, summary FROM file_index WHERE folder_id = #{folderId}")
    List<FileIndex> selectByFolderId(@Param("folderId") Long folderId);
    
    /**
     * 全文搜索
     */
    @Select("SELECT id, file_path, file_name, file_size, file_type, last_modified, indexed_time, folder_id, content, summary FROM file_index WHERE content LIKE '%' || #{query} || '%' OR file_name LIKE '%' || #{query} || '%'")
    List<FileIndex> searchByContent(@Param("query") String query);
    
    /**
     * 分页搜索
     */
    @Select("SELECT id, file_path, file_name, file_size, file_type, last_modified, indexed_time, folder_id, content, summary FROM file_index WHERE content LIKE '%' || #{query} || '%' OR file_name LIKE '%' || #{query} || '%'")
    IPage<FileIndex> searchByContentPage(Page<FileIndex> page, @Param("query") String query);
    
    /**
     * 获取所有文件（不包含vector_data字段，避免BLOB读取问题）
     */
    @Select("SELECT id, file_path, file_name, file_size, file_type, last_modified, indexed_time, folder_id, content, summary FROM file_index")
    List<FileIndex> selectAllWithoutVectorData();
}
