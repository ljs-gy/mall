package org.dromara.goods.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品类型发布模板（品牌/规格/参数/属性聚合）
 *
 * @author ljs
 * @date 2026-07-28
 */
@Data
public class TypeTemplateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long typeId;

    private String typeName;

    private List<BrandVo> brands = new ArrayList<>();

    private List<SpecGroupTemplateVo> specGroups = new ArrayList<>();

    private List<ParameterVo> parameters = new ArrayList<>();

    private List<AttributeTemplateVo> attributes = new ArrayList<>();

    @Data
    public static class SpecGroupTemplateVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long id;
        private String specGroupName;
        private List<SpecTemplateVo> specs = new ArrayList<>();
    }

    @Data
    public static class SpecTemplateVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long id;
        private String specName;
        private List<SpecOptionVo> options = new ArrayList<>();
    }

    @Data
    public static class AttributeTemplateVo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long id;
        private String attributeName;
        private List<AttributeOptionVo> options = new ArrayList<>();
    }
}
