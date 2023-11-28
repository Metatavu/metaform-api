<div>
<#list metaform.getSections() as section>
  <div>
    <h3>${section.getTitle()!''}</h3>
    <#list section.getFields() as field>
      <#assign fieldName = field.getName()>
      <#if fieldName?has_content>
        <#if field.getType()?has_content>
          <div class="reply-field">
            <#switch field.getType()!'unknown'>
              <#case 'text'>
              <#case 'number'>
              <#case 'email'>
              <#case 'memo'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <div>${fieldValue?html?replace("\n", "<br/>")}</div>
                </#if>
              <#break>
              <#case 'date'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <#attempt>
                    ${fieldValue?date.iso}
                  <#recover>
                    ${fieldValue}
                  </#attempt>
                </#if>
              <#break>
              <#case 'time'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <#attempt>
                    ${fieldValue?time.iso}
                  <#recover>
                    ${fieldValue}
                  </#attempt>
                </#if>
              <#break>
              <#case 'date-time'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <#attempt>
                    ${fieldValue?datetime.iso}
                  <#recover>
                    ${fieldValue}
                  </#attempt>
                </#if>
              <#break>
              <#case 'radio'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <#if field.getOptions()??>
                    <#list field.getOptions() as option>
                      <#if option.getName() == fieldValue>
                        <#assign optionText = option.getText()>
                      </#if>
                    </#list>
                    <div><label><b>${field.getTitle()!''}</b></label></div>
                    <div>${optionText}</div>
                  </#if>
                </#if>
              <#break>
              <#case 'select'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <#if field.getOptions()??>
                    <#list field.getOptions() as option>
                      <#if option.getName() == fieldValue>
                        <#assign optionText = option.getText()>
                      </#if>
                    </#list>
                    <div><label><b>${field.getTitle()!''}</b></label></div>
                    <div>${optionText}</div>
                  </#if>
                </#if>
              <#break>
              <#case 'autocomplete'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <#if field.getOptions()??>
                    <#list field.getOptions() as option>
                      <#if option.getName() == fieldValue>
                        <#assign optionText = option.getText()>
                      </#if>
                    </#list>
                    <div><label><b>${field.getTitle()!''}</b></label></div>
                    <div>${optionText}</div>
                  </#if>
                </#if>
              <#break>
              <#case 'table'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <#if field.getColumns()??>
                    <div><label><b>${field.getTitle()!''}</b></label></div>
                    <table style="width: 100%">
                      <tr>
                        <#list field.getColumns() as column>
                          <th>${column.getTitle()!''}</th>
                        </#list>
                      </tr>                                      
                      <#list fieldValue as row>
                        <#if row??>
                          <tr>
                            <#list field.getColumns() as column>
                              <td>${row[column.getName()]!''}</td>
                            </#list>
                          </tr>
                        </#if>
                      </#list>
                    </table>
                  </#if>
                </#if>
              <#break>
              <#case 'boolean'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><b>[X]</b> ${field.getText()!''}</div>
                </#if>
                <#if !fieldValue?has_content>
                  <div><b>[_]</b> ${field.getText()!''}</div>
                </#if>
              <#break>
              <#case 'checklist'>
                <div><label><b>${field.getTitle()!''}</b></label></div>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <#assign checked = fieldValue?split(",")>
                  <#if field.getOptions()??>
                    <#list field.getOptions() as option>
                      <#assign optionName = option.getName()!''>
                      <div><b>[${checked?seq_contains(optionName)?string("X", "_")}]</b> ${option.getText()!''}</div>
                    </#list>
                  </#if>
                </#if>
              <#break>
              <#case 'files'>
                <#if reply.getData()[fieldName]??>
                  <#list reply.getData()[fieldName] as attachmentId>
                    <#if attachments[attachmentId]??>
                      <pre>${attachments[attachmentId].getName()}</pre>
                    </#if>
                  </#list>
                </#if>
              <#break>
              <#case 'html'>
              <#case 'hidden'>
              <#case 'submit'>
              <#break>
              <#default>
                <pre style="color: red">Unknown field type ${field.getType()}</pre>
            </#switch>
          </div>
        </#if>
      </#if>
    </#list>
  </div>
</#list>
</div>