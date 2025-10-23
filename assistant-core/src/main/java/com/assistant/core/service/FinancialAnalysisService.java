package com.assistant.core.service;

import com.assistant.core.entity.FileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 财务分析服务 - 专门用于分析利润情况和财务数据
 */
@Service
public class FinancialAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(FinancialAnalysisService.class);

    @Autowired
    private AIEmbeddingService aiEmbeddingService;

    /**
     * 生成财务分析总结
     */
    public String generateFinancialAnalysis(String query, List<FileIndex> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "未找到相关财务文件，无法进行财务分析。";
        }

        try {
            logger.info("开始生成财务分析，查询: {}, 文件数量: {}", query, searchResults.size());
            
            StringBuilder analysis = new StringBuilder();
            analysis.append("## 💰 财务分析报告\n\n");
            
            // 基本信息
            analysis.append("**分析主题**: ").append(query).append("\n");
            analysis.append("**分析时间**: ").append(java.time.LocalDateTime.now().toString()).append("\n");
            analysis.append("**相关文件**: ").append(searchResults.size()).append(" 个\n\n");
            
            // 财务数据分析
            FinancialData financialData = extractFinancialData(searchResults);
            analysis.append("### 📊 财务数据分析\n");
            analysis.append(generateFinancialSummary(financialData)).append("\n\n");
            
            // 利润情况分析
            analysis.append("### 💹 利润情况分析\n");
            analysis.append(generateProfitAnalysis(searchResults, financialData)).append("\n\n");
            
            // 关键财务指标
            analysis.append("### 📈 关键财务指标\n");
            analysis.append(generateKeyMetrics(searchResults)).append("\n\n");
            
            // 财务趋势分析
            analysis.append("### 📉 财务趋势分析\n");
            analysis.append(generateTrendAnalysis(searchResults)).append("\n\n");
            
            // 风险评估
            analysis.append("### ⚠️ 财务风险评估\n");
            analysis.append(generateRiskAssessment(searchResults)).append("\n\n");
            
            // 相关文件列表
            analysis.append("### 📁 相关财务文件\n");
            for (int i = 0; i < Math.min(searchResults.size(), 5); i++) {
                FileIndex file = searchResults.get(i);
                analysis.append(i + 1).append(". **").append(file.getFileName()).append("**\n");
                analysis.append("   - 类型: ").append(file.getFileType()).append("\n");
                analysis.append("   - 大小: ").append(formatFileSize(file.getFileSize())).append("\n\n");
            }
            
            logger.info("财务分析生成完成，长度: {}", analysis.length());
            return analysis.toString();
            
        } catch (Exception e) {
            logger.error("生成财务分析失败", e);
            return "生成财务分析时发生错误: " + e.getMessage();
        }
    }

    /**
     * 提取财务数据
     */
    private FinancialData extractFinancialData(List<FileIndex> searchResults) {
        FinancialData data = new FinancialData();
        
        for (FileIndex file : searchResults) {
            if (file.getContent() != null && !file.getContent().trim().isEmpty()) {
                String content = file.getContent();
                
                // 提取收入数据
                extractRevenueData(content, data);
                
                // 提取成本数据
                extractCostData(content, data);
                
                // 提取利润数据
                extractProfitData(content, data);
                
                // 提取资产数据
                extractAssetData(content, data);
            }
        }
        
        return data;
    }

    /**
     * 提取收入数据
     */
    private void extractRevenueData(String content, FinancialData data) {
        // 匹配收入相关的数字
        Pattern revenuePattern = Pattern.compile("(?:收入|营收|营业收入|销售收入)[：:：]?\\s*[￥$]?([0-9,，.]+)(?:万|千|亿)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = revenuePattern.matcher(content);
        
        while (matcher.find()) {
            String amount = matcher.group(1).replaceAll("[，,]", "");
            try {
                double value = Double.parseDouble(amount);
                data.addRevenue(value);
            } catch (NumberFormatException e) {
                // 忽略无法解析的数字
            }
        }
    }

    /**
     * 提取成本数据
     */
    private void extractCostData(String content, FinancialData data) {
        // 匹配成本相关的数字
        Pattern costPattern = Pattern.compile("(?:成本|费用|支出)[：:：]?\\s*[￥$]?([0-9,，.]+)(?:万|千|亿)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = costPattern.matcher(content);
        
        while (matcher.find()) {
            String amount = matcher.group(1).replaceAll("[，,]", "");
            try {
                double value = Double.parseDouble(amount);
                data.addCost(value);
            } catch (NumberFormatException e) {
                // 忽略无法解析的数字
            }
        }
    }

    /**
     * 提取利润数据
     */
    private void extractProfitData(String content, FinancialData data) {
        // 匹配利润相关的数字
        Pattern profitPattern = Pattern.compile("(?:利润|净利润|毛利润|营业利润)[：:：]?\\s*[￥$]?([0-9,，.]+)(?:万|千|亿)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = profitPattern.matcher(content);
        
        while (matcher.find()) {
            String amount = matcher.group(1).replaceAll("[，,]", "");
            try {
                double value = Double.parseDouble(amount);
                data.addProfit(value);
            } catch (NumberFormatException e) {
                // 忽略无法解析的数字
            }
        }
    }

    /**
     * 提取资产数据
     */
    private void extractAssetData(String content, FinancialData data) {
        // 匹配资产相关的数字
        Pattern assetPattern = Pattern.compile("(?:资产|总资产|净资产)[：:：]?\\s*[￥$]?([0-9,，.]+)(?:万|千|亿)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = assetPattern.matcher(content);
        
        while (matcher.find()) {
            String amount = matcher.group(1).replaceAll("[，,]", "");
            try {
                double value = Double.parseDouble(amount);
                data.addAsset(value);
            } catch (NumberFormatException e) {
                // 忽略无法解析的数字
            }
        }
    }

    /**
     * 生成财务总结
     */
    private String generateFinancialSummary(FinancialData data) {
        StringBuilder summary = new StringBuilder();
        
        if (!data.getRevenues().isEmpty()) {
            double avgRevenue = data.getRevenues().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            summary.append("**平均收入**: ").append(String.format("%.2f万元", avgRevenue)).append("\n");
        }
        
        if (!data.getCosts().isEmpty()) {
            double avgCost = data.getCosts().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            summary.append("**平均成本**: ").append(String.format("%.2f万元", avgCost)).append("\n");
        }
        
        if (!data.getProfits().isEmpty()) {
            double avgProfit = data.getProfits().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            summary.append("**平均利润**: ").append(String.format("%.2f万元", avgProfit)).append("\n");
        }
        
        if (!data.getAssets().isEmpty()) {
            double avgAsset = data.getAssets().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            summary.append("**平均资产**: ").append(String.format("%.2f万元", avgAsset)).append("\n");
        }
        
        return summary.toString();
    }

    /**
     * 生成利润情况分析
     */
    private String generateProfitAnalysis(List<FileIndex> searchResults, FinancialData data) {
        StringBuilder analysis = new StringBuilder();
        
        if (!data.getProfits().isEmpty()) {
            double maxProfit = data.getProfits().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double minProfit = data.getProfits().stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double avgProfit = data.getProfits().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            analysis.append("**利润情况概览**:\n");
            analysis.append("- 最高利润: ").append(String.format("%.2f万元", maxProfit)).append("\n");
            analysis.append("- 最低利润: ").append(String.format("%.2f万元", minProfit)).append("\n");
            analysis.append("- 平均利润: ").append(String.format("%.2f万元", avgProfit)).append("\n\n");
            
            // 利润趋势分析
            if (maxProfit > avgProfit) {
                analysis.append("**趋势分析**: 利润表现良好，最高利润超过平均水平。\n");
            } else if (minProfit < avgProfit) {
                analysis.append("**趋势分析**: 利润存在波动，需要关注最低利润情况。\n");
            } else {
                analysis.append("**趋势分析**: 利润相对稳定，保持在平均水平。\n");
            }
        } else {
            analysis.append("**利润情况**: 未找到具体的利润数据，建议查看财务报表获取详细信息。\n");
        }
        
        return analysis.toString();
    }

    /**
     * 生成关键指标
     */
    private String generateKeyMetrics(List<FileIndex> searchResults) {
        StringBuilder metrics = new StringBuilder();
        
        // 查找关键财务指标
        for (FileIndex file : searchResults.subList(0, Math.min(3, searchResults.size()))) {
            if (file.getContent() != null && !file.getContent().trim().isEmpty()) {
                String content = file.getContent();
                
                // 查找毛利率
                Pattern grossMarginPattern = Pattern.compile("(?:毛利率|毛利润率)[：:：]?\\s*([0-9.]+)%?", Pattern.CASE_INSENSITIVE);
                Matcher matcher = grossMarginPattern.matcher(content);
                if (matcher.find()) {
                    metrics.append("- **毛利率**: ").append(matcher.group(1)).append("%\n");
                }
                
                // 查找净利率
                Pattern netMarginPattern = Pattern.compile("(?:净利率|净利润率)[：:：]?\\s*([0-9.]+)%?", Pattern.CASE_INSENSITIVE);
                matcher = netMarginPattern.matcher(content);
                if (matcher.find()) {
                    metrics.append("- **净利率**: ").append(matcher.group(1)).append("%\n");
                }
                
                // 查找ROE
                Pattern roePattern = Pattern.compile("(?:ROE|净资产收益率)[：:：]?\\s*([0-9.]+)%?", Pattern.CASE_INSENSITIVE);
                matcher = roePattern.matcher(content);
                if (matcher.find()) {
                    metrics.append("- **净资产收益率(ROE)**: ").append(matcher.group(1)).append("%\n");
                }
            }
        }
        
        if (metrics.length() == 0) {
            metrics.append("未找到具体的财务指标数据，建议查看详细的财务报表。");
        }
        
        return metrics.toString();
    }

    /**
     * 生成趋势分析
     */
    private String generateTrendAnalysis(List<FileIndex> searchResults) {
        StringBuilder trend = new StringBuilder();
        
        // 查找时间序列数据
        for (FileIndex file : searchResults.subList(0, Math.min(2, searchResults.size()))) {
            if (file.getContent() != null && !file.getContent().trim().isEmpty()) {
                String content = file.getContent();
                
                // 查找年度数据
                Pattern yearPattern = Pattern.compile("(?:202[0-9]年|202[0-9])", Pattern.CASE_INSENSITIVE);
                Matcher matcher = yearPattern.matcher(content);
                if (matcher.find()) {
                    trend.append("发现").append(matcher.group(0)).append("的财务数据，可用于趋势分析。\n");
                }
            }
        }
        
        if (trend.length() == 0) {
            trend.append("建议查看多年度财务报表以进行趋势分析。");
        }
        
        return trend.toString();
    }

    /**
     * 生成风险评估
     */
    private String generateRiskAssessment(List<FileIndex> searchResults) {
        StringBuilder risk = new StringBuilder();
        
        // 查找风险相关关键词
        for (FileIndex file : searchResults.subList(0, Math.min(3, searchResults.size()))) {
            if (file.getContent() != null && !file.getContent().trim().isEmpty()) {
                String content = file.getContent();
                
                if (content.contains("风险") || content.contains("风险点") || content.contains("风险提示")) {
                    risk.append("发现风险相关文档，建议详细查看风险提示内容。\n");
                }
            }
        }
        
        if (risk.length() == 0) {
            risk.append("未发现明显的风险提示，建议进行全面的风险评估。");
        }
        
        return risk.toString();
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null) return "未知";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
        return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * 财务数据类
     */
    private static class FinancialData {
        private List<Double> revenues = new ArrayList<>();
        private List<Double> costs = new ArrayList<>();
        private List<Double> profits = new ArrayList<>();
        private List<Double> assets = new ArrayList<>();

        public void addRevenue(double revenue) { revenues.add(revenue); }
        public void addCost(double cost) { costs.add(cost); }
        public void addProfit(double profit) { profits.add(profit); }
        public void addAsset(double asset) { assets.add(asset); }

        public List<Double> getRevenues() { return revenues; }
        public List<Double> getCosts() { return costs; }
        public List<Double> getProfits() { return profits; }
        public List<Double> getAssets() { return assets; }
    }
}
