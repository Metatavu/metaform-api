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
              <#case 'html'>
              <#case 'memo'>
                <#assign fieldValue = reply.getData()[fieldName]!''>
                <#if fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <div>${fieldValue?html?replace("\n", "<br/>")}</div>
                </#if>
              <#break>
            <#case 'url'>
              <#assign fieldValue = reply.getData()[fieldName]!''>
              <#if fieldValue?has_content>
                <div><label><b>${field.getTitle()!''}</b></label></div>
                <a href="${fieldValue}">${fieldValue}</a>
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
                    <div>${optionText!''}</div>
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
                    <div>${optionText!''}</div>
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
                <#assign table = field.table>
                <#if fieldValue?has_content>
                  <#if table.getColumns()??>
                    <div><label><b>${field.getTitle()!''}</b></label></div>
                    <table style="width: 100%">
                      <tr>
                        <#list table.getColumns() as column>
                          <th>${column.getTitle()!''}</th>
                        </#list>
                      </tr>                                      
                      <#list fieldValue as row>
                        <#if row??>
                          <tr>
                            <#list table.getColumns() as column>
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
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <div><b>[X]</b> ${field.getText()!''}</div>
                </#if>
                <#if !fieldValue?has_content>
                  <div><label><b>${field.getTitle()!''}</b></label></div>
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
                  <div><label><b>${field.getTitle()!''}</b></label></div>
                  <#list reply.getData()[fieldName] as attachmentId>
                    <#if attachments[attachmentId]??>
                      <pre>${attachments[attachmentId].getName()}</pre>
                    </#if>
                  </#list>
                </#if>
              <#break>
                <#case 'slider'>
                  <#if reply.getData()[fieldName]??>
                  <#assign fieldValue = reply.getData()[fieldName]!''>
                    <#if fieldValue?has_content>
                      <div>
                        <div><label><b>${field.getTitle()!''}</b></label></div>
                        <div class="slider-container">
                         <div>
                           ${field.getMin()!0}<input type="range" min="${field.getMin()!0}" max="${field.getMax()!100}" value="${fieldValue}"> ${field.getMax()!100}
                         </div>
                          ${fieldValue}
                        <div>
                      </div>
                    </#if>
                  </#if>
                <#break>
              <#case 'hidden'>
              <#case 'small-text'>
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