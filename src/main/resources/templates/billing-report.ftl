<!DOCTYPE html>
<head>
  <meta charset="UTF-8">
  <style>
    body {
      margin: 5rem;
    }
    .header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5rem;
    }
    .total {
      margin-bottom: 5rem;
    }
    .table-header {
      width: 15rem;
      text-align: start;
    }
    .summary {
      margin-bottom: 5rem;
    }
    .summary-header {
      display: flex;
      flex-direction: row;
      column-gap: 5rem;
      font-weight: bold;
    }
    .footer {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
    }
  </style>
</head>
<body>
  <div class="header">
    <img src="https://s3.eu-central-1.amazonaws.com/static.metatavu.io/metaform/metatavu/metatavu-logo-dark.svg" />
    <p style="display: inline">Eloisa Metaform</p>
  </div>
  <div class="total">
    <p>${from} - ${to}</p>
    <p>Kuukausilaskuja yhteensä: ${totalInvoices}</p>
    <table style="border-collapse: seperate; border-spacing: 0.5rem;">
      <tr class="table-row">
        <th class="table-header">Artikelli</th>
        <th class="table-header">Määrä</th>
        <th class="table-header">Yksikköhinta</th>
        <th class="table-header">Kokonaishinta</th>
        <th></th>
      </tr>
<#assign strongAuthenticationTotalCost = strongAuthenticationCount * strongAuthenticationCost>
<#assign formsTotalCost = formsCount * formCost>
<#assign managersTotalCost = managersCount * managerCost>
<#assign adminsTotalCost = adminsCount * adminCost>
      <tr>
        <td>Suomi.fi</td>
        <td>${strongAuthenticationCount}</td>
        <td>${strongAuthenticationCost} €</td>
        <td>${strongAuthenticationTotalCost} €</td>
      </tr>
      <tr>
        <td>Lomakkeet</td>
        <td>${formsCount}</td>
        <td>${formCost} €</td>
        <td>${formsTotalCost} €</td>
      </tr>
      <tr>
        <td>Käsittelijät</td>
        <td>${managersCount}</td>
        <td>${managerCost} €</td>
        <td>${managersTotalCost} €</td>
      </tr>
      <tr>
        <td>Ylläpitäjät</td>
        <td>${adminsCount}</td>
        <td>${adminCost} €</td>
        <td>${adminsTotalCost} €</td>
      </tr>
      <tr>
        <td colspan="3"></td>
        <td>
          ${strongAuthenticationTotalCost + formsTotalCost + managersTotalCost + adminsTotalCost} €
        </td>
        <td>${(strongAuthenticationTotalCost + formsTotalCost + managersTotalCost + adminsTotalCost) * 1.24} €</td>
      </tr>
    </table>
  </div>
  <div class="summary">
    <table style="border-collapse: seperate; border-spacing: 0.5rem;">
      <tr>
        <th class="table-header">
          Lomakkeet
        </th>
        <th class="table-header">
          Suomi.fi
        </th>
        <th class="table-header">
          Käsittelijät
        </th>
        <th class="table-header">
          Käsittelijäryhmät
        </th>
      </tr>
      <#list forms as form>
        <tr>
          <td>
            ${form.title}
          </td>
          <td>
            ${form.strongAuthentication?then("Kyllä", "Ei")}
          </td>
          <td>
            ${form.managersCount}
          </td>
          <td>
            ${form.groupsCount}
          </td>
        </tr>
      </#list>
    </table>
  </div>
  <hr/>
  <div class="footer">
    <div>
      <p>Metatavu Oy</p>
      <p>Hallituskatu 7 A, 2krs</p>
      <p>50100 Mikkeli</p>
    </div>
    <div>
      <p>info@metatavu.fi</p>
      <p>+358 10 579 3710</p>
    </div>
  </div>
</body>
</html>