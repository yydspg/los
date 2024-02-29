package ${package.Mapper};

import com.los.core.entity.${entity};
import ${superMapperClassPackage};
<#if mapperAnnotationClass??>
    import ${mapperAnnotationClass.name};
</#if>

/*
* <p>
    * ${table.comment!} Mapper 接口
    * </p>
*
* @author ${author}
* @since ${date}
*/
<#if mapperAnnotationClass??>
    @${mapperAnnotationClass.simpleName}
</#if>
<#if kotlin>
    interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
    public interface ${table.mapperName} extends ${superMapperClass}<${entity}> {

    }
</#if>
