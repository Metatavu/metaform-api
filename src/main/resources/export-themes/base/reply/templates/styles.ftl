<style type="text/css">
  @page {
    margin-top: 160px;
    margin-bottom: 140px;
    padding: 0;
    margin-left: 0;
    margin-right: 0;
    @top-center { content: element(header) };
    @bottom-center { content: element(footer) };
  }
  
  .header-img {
    width: 249px;
    height: 62px;
    display: block;
  }

  .page-number:after {
    content: counter(page) " / " counter(pages);
  }
  
  .bt {
    border-top: 1px solid #000;
  }
  
  .bb {
    border-bottom: 1px solid #000;
  }
  
  .header {
    display: block;
    margin-top: 20px;
    margin-left: 20px;
    padding-right: 20px;
    margin-bottom: 10px;
    padding-bottom: 10px;
    position: running(header);
  }

  .header-time {
    float: right;
  }
  
  .footer {
    display: block;
    margin-top: 20px;
    margin-left: 20px;
    padding-right: 20px;
    margin-bottom: 10px;
    padding-bottom: 10px;
    position: running(footer);
  }
  
  .content {
    padding-left: 20px;
    padding-right: 20px;
  }

  body, html {
    font-family: arial;
    padding: 0;
    margin: 0;
  }
  
  .row {
    width: 100%;
    display: block;
    box-sizing: border-box;
    overflow: hidden;
    white-space: nowrap;
    margin: 0;
    padding: 0;
    clear: both;
  }
  
  .mt-1 {
    margin-top: 10px;
  }
  
  .mt-2 {
    margin-top: 20px;
  }
  
  .mt-3 {
    margin-top: 30px;
  }
  
  .mt-4 {
    margin-top: 40px;
  }
  
  .pt-1 {
    padding-top: 10px;
  }
  
  .pt-2 {
    padding-top: 20px;
  }
  
  .pt-3 {
    padding-top: 30px;
  }
  
  .pl-1 {
    padding-left: 10px;
  }
  
  .col-1, .col-2, .col-3, .col-4, .col-5, .col-6, .col-7, .col-8, .col-9, .col-10, .col-11, .col-12 {
    display: block;
    vertical-align: top;
    float: left;
    overflow: hidden;
    box-sizing: border-box;
  }

  .col-1 {
    width: 8.333333333333334%;
  }
      
  .col-2 {
    width: 16.666666666666668%;
  }
      
  .col-3 {
    width: 25%;
  }
      
  .col-4 {
    width: 33.333333333333336%;
  }
      
  .col-5 {
    width: 41.66666666666667%;
  }
      
  .col-6 {
    width: 50%;
  }
      
  .col-7 {
    width: 58.333333333333336%;
  }
      
  .col-8 {
    width: 66.66666666666667%;
  }
      
  .col-9 {
    width: 75%;
  }
      
  .col-10 {
    width: 83.33333333333334%;
  }
      
  .col-11 {
    width: 91.66666666666667%;
  }
      
  .col-12 {
    width: 100%;
  }
  
  .ts-2 {
    font-size: 14px;
  }
  
  .ts-3 {
    font-size: 15px;
  }
  
  .ts-4 {
    font-size: 16px;
  }
  
  .ts-5 {
    font-size: 17px;
  }
  
  .ts-6 {
    font-size: 18px;
  }
  
  .ts-7 {
    font-size: 20px;
  }
  
  .ts-8 {
    font-size: 22px;
  }
  
  .text-right {
    text-align: right;
  }
  
  .text-center {
    text-align: center;
  }
  
  .reply-field {
    margin-bottom: 10px;
  }

  .slider-container {
    display: flex;
      flex-direction: column;
      align-items: center;
  }
  
</style>