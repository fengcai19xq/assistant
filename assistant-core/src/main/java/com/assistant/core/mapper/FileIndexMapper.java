package com.assistant.core.mapper;

import com.assistant.core.entity.FileIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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
    
    /**
     * 获取文件类型分布
     */
    @Select("SELECT file_type, COUNT(*) as count FROM file_index GROUP BY file_type")
    List<Map<String, Object>> getFileTypeDistribution();
    
    /**
     * 获取文件大小分布
     */
    @Select("SELECT CASE WHEN file_size < 1024*1024 THEN 'small' WHEN file_size < 10*1024*1024 THEN 'medium' ELSE 'large' END as size_category, COUNT(*) as count FROM file_index GROUP BY size_category")
    List<Map<String, Object>> getFileSizeDistribution();
    
    /**
     * 获取修改时间分布
     */
    @Select("SELECT CASE WHEN last_modified > datetime('now', '-1 day') THEN 'recent' WHEN last_modified > datetime('now', '-7 days') THEN 'week' WHEN last_modified > datetime('now', '-30 days') THEN 'month' ELSE 'old' END as time_category, COUNT(*) as count FROM file_index GROUP BY time_category")
    List<Map<String, Object>> getModificationTimeDistribution();
    
    /**
     * 获取慢查询
     */
    @Select("SELECT sql FROM sqlite_master WHERE type='table' AND name='file_index'")
    List<String> getSlowQueries();
    
    /**
     * 优化查询计划
     */
    @Select("ANALYZE file_index")
    void optimizeQueryPlan();
    
    /**
     * 创建内容索引
     */
    @Select("CREATE INDEX IF NOT EXISTS idx_content ON file_index(content)")
    void createContentIndex();
    
    /**
     * 创建文件类型索引
     */
    @Select("CREATE INDEX IF NOT EXISTS idx_file_type ON file_index(file_type)")
    void createFileTypeIndex();
    
    /**
     * 创建修改时间索引
     */
    @Select("CREATE INDEX IF NOT EXISTS idx_last_modified ON file_index(last_modified)")
    void createModificationTimeIndex();
    
    /**
     * 创建文件大小索引
     */
    @Select("CREATE INDEX IF NOT EXISTS idx_file_size ON file_index(file_size)")
    void createFileSizeIndex();
    
    /**
     * 更新表统计信息
     */
    @Select("ANALYZE file_index")
    void updateTableStats();
    
    /**
     * 删除无效索引
     */
    @Select("DELETE FROM file_index WHERE file_path NOT IN (SELECT file_path FROM file_index WHERE file_path IS NOT NULL)")
    int deleteInvalidIndexes();
    
    /**
     * 获取索引大小
     */
    @Select("SELECT COUNT(*) FROM file_index")
    long getIndexSize();
}
