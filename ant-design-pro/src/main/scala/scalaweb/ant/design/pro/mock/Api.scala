package scalaweb.ant.design.pro.mock

object Api {
  val variables = """const titles = [
                    |  'Alipay',
                    |  'Angular',
                    |  'Ant Design',
                    |  'Ant Design Pro',
                    |  'Bootstrap',
                    |  'React',
                    |  'Vue',
                    |  'Webpack',
                    |];
                    |const avatars = [
                    |  'https://gw.alipayobjects.com/zos/rmsportal/WdGqmHpayyMjiEhcKoVE.png', // Alipay
                    |  'https://gw.alipayobjects.com/zos/rmsportal/zOsKZmFRdUtvpqCImOVY.png', // Angular
                    |  'https://gw.alipayobjects.com/zos/rmsportal/dURIMkkrRFpPgTuzkwnB.png', // Ant Design
                    |  'https://gw.alipayobjects.com/zos/rmsportal/sfjbOqnsXXJgNCjCzDBL.png', // Ant Design Pro
                    |  'https://gw.alipayobjects.com/zos/rmsportal/siCrBXXhmvTQGWPNLBow.png', // Bootstrap
                    |  'https://gw.alipayobjects.com/zos/rmsportal/kZzEzemZyKLKFsojXItE.png', // React
                    |  'https://gw.alipayobjects.com/zos/rmsportal/ComBAopevLwENQdKWiIn.png', // Vue
                    |  'https://gw.alipayobjects.com/zos/rmsportal/nxkuOJlFJuAUhzlMTCEe.png', // Webpack
                    |];
                    |
                    |const avatars2 = [
                    |  'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/cnrhVkzwxjPwAaCfPbdc.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/gaOngJwsRYRaVAuXXcmB.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/ubnKSIfAJTxIgXOKlciN.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/WhxKECPNujWoWEFNdnJE.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/jZUIxmJycoymBprLOUbT.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/psOgztMplJMGpVEqfcgF.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/ZpBqSxLxVEXfcUNoPKrz.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/laiEnJdGHVOhJrUShBaJ.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/UrQsqscbKEpNuJcvBZBu.png',
                    |];
                    |
                    |const covers = [
                    |  'https://gw.alipayobjects.com/zos/rmsportal/uMfMFlvUuceEyPpotzlq.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/iZBVOIhGJiAnhplqjvZW.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/iXjVmWVHbCJAyqvDxdtx.png',
                    |  'https://gw.alipayobjects.com/zos/rmsportal/gLaIAoVWTtLbBWZNYEMg.png',
                    |];
                    |const desc = [
                    |  '那是一种内在的东西， 他们到达不了，也无法触及的',
                    |  '希望是一个好东西，也许是最好的，好东西是不会消亡的',
                    |  '生命就像一盒巧克力，结果往往出人意料',
                    |  '城镇中有那么多的酒馆，她却偏偏走进了我的酒馆',
                    |  '那时候我只会想自己想要什么，从不想自己拥有什么',
                    |];
                    |
                    |const user = [
                    |  '付小小',
                    |  '曲丽丽',
                    |  '林东东',
                    |  '周星星',
                    |  '吴加好',
                    |  '朱偏右',
                    |  '鱼酱',
                    |  '乐哥',
                    |  '谭小仪',
                    |  '仲尼',
                    |];
                    |""".stripMargin

  // #currentUser
  val currentUser =
    """{"name":"羊八井","avatar":"https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png","userid":"00000001","email":"yangbajing@gmail.com","signature":"海纳百川，有容乃大","title":"一个好爸爸","group":"华龙海数－某某某事业群－某某平台部－某某技术部－Developer","tags":[{"key":"0","label":"很有想法的"},{"key":"1","label":"专注后端"},{"key":"2","label":"强~"},{"key":"3","label":"彪悍"},{"key":"4","label":"重庆崽儿"},{"key":"5","label":"海纳百川"}],"notifyCount":12,"country":"China","geographic":{"province":{"label":"重庆市","key":"330000"},"city":{"label":"渝北区","key":"402260"}},"address":"渝北区金开大道西段106号10栋移动新媒体产业大厦11楼","phone":"023-88888888"}"""
  // #currentUser

  val fake_chart_data =
    """{"visitData":[{"x":"2018-10-09","y":7},{"x":"2018-10-10","y":5},{"x":"2018-10-11","y":4},{"x":"2018-10-12","y":2},{"x":"2018-10-13","y":4},{"x":"2018-10-14","y":7},{"x":"2018-10-15","y":5},{"x":"2018-10-16","y":6},{"x":"2018-10-17","y":5},{"x":"2018-10-18","y":9},{"x":"2018-10-19","y":6},{"x":"2018-10-20","y":3},{"x":"2018-10-21","y":1},{"x":"2018-10-22","y":5},{"x":"2018-10-23","y":3},{"x":"2018-10-24","y":6},{"x":"2018-10-25","y":5}],"visitData2":[{"x":"2018-10-09","y":1},{"x":"2018-10-10","y":6},{"x":"2018-10-11","y":4},{"x":"2018-10-12","y":8},{"x":"2018-10-13","y":3},{"x":"2018-10-14","y":7},{"x":"2018-10-15","y":2}],"salesData":[{"x":"1月","y":1161},{"x":"2月","y":710},{"x":"3月","y":221},{"x":"4月","y":1015},{"x":"5月","y":1071},{"x":"6月","y":279},{"x":"7月","y":668},{"x":"8月","y":1195},{"x":"9月","y":1097},{"x":"10月","y":752},{"x":"11月","y":443},{"x":"12月","y":1123}],"searchData":[{"index":1,"keyword":"搜索关键词-0","count":855,"range":81,"status":0},{"index":2,"keyword":"搜索关键词-1","count":739,"range":36,"status":0},{"index":3,"keyword":"搜索关键词-2","count":286,"range":26,"status":0},{"index":4,"keyword":"搜索关键词-3","count":886,"range":42,"status":0},{"index":5,"keyword":"搜索关键词-4","count":125,"range":71,"status":1},{"index":6,"keyword":"搜索关键词-5","count":134,"range":65,"status":0},{"index":7,"keyword":"搜索关键词-6","count":206,"range":47,"status":0},{"index":8,"keyword":"搜索关键词-7","count":100,"range":21,"status":0},{"index":9,"keyword":"搜索关键词-8","count":73,"range":99,"status":0},{"index":10,"keyword":"搜索关键词-9","count":204,"range":88,"status":1},{"index":11,"keyword":"搜索关键词-10","count":940,"range":75,"status":0},{"index":12,"keyword":"搜索关键词-11","count":600,"range":11,"status":1},{"index":13,"keyword":"搜索关键词-12","count":683,"range":87,"status":0},{"index":14,"keyword":"搜索关键词-13","count":629,"range":91,"status":0},{"index":15,"keyword":"搜索关键词-14","count":799,"range":8,"status":0},{"index":16,"keyword":"搜索关键词-15","count":312,"range":12,"status":0},{"index":17,"keyword":"搜索关键词-16","count":216,"range":81,"status":0},{"index":18,"keyword":"搜索关键词-17","count":301,"range":48,"status":0},{"index":19,"keyword":"搜索关键词-18","count":874,"range":78,"status":1},{"index":20,"keyword":"搜索关键词-19","count":61,"range":10,"status":1},{"index":21,"keyword":"搜索关键词-20","count":219,"range":53,"status":1},{"index":22,"keyword":"搜索关键词-21","count":231,"range":35,"status":1},{"index":23,"keyword":"搜索关键词-22","count":410,"range":26,"status":1},{"index":24,"keyword":"搜索关键词-23","count":439,"range":52,"status":1},{"index":25,"keyword":"搜索关键词-24","count":465,"range":47,"status":1},{"index":26,"keyword":"搜索关键词-25","count":96,"range":25,"status":0},{"index":27,"keyword":"搜索关键词-26","count":296,"range":52,"status":0},{"index":28,"keyword":"搜索关键词-27","count":101,"range":44,"status":0},{"index":29,"keyword":"搜索关键词-28","count":503,"range":79,"status":1},{"index":30,"keyword":"搜索关键词-29","count":692,"range":78,"status":1},{"index":31,"keyword":"搜索关键词-30","count":41,"range":33,"status":0},{"index":32,"keyword":"搜索关键词-31","count":442,"range":12,"status":0},{"index":33,"keyword":"搜索关键词-32","count":575,"range":62,"status":1},{"index":34,"keyword":"搜索关键词-33","count":339,"range":10,"status":0},{"index":35,"keyword":"搜索关键词-34","count":476,"range":3,"status":0},{"index":36,"keyword":"搜索关键词-35","count":995,"range":18,"status":1},{"index":37,"keyword":"搜索关键词-36","count":728,"range":43,"status":0},{"index":38,"keyword":"搜索关键词-37","count":615,"range":69,"status":0},{"index":39,"keyword":"搜索关键词-38","count":898,"range":83,"status":0},{"index":40,"keyword":"搜索关键词-39","count":534,"range":56,"status":1},{"index":41,"keyword":"搜索关键词-40","count":323,"range":69,"status":0},{"index":42,"keyword":"搜索关键词-41","count":303,"range":2,"status":1},{"index":43,"keyword":"搜索关键词-42","count":638,"range":21,"status":1},{"index":44,"keyword":"搜索关键词-43","count":338,"range":25,"status":1},{"index":45,"keyword":"搜索关键词-44","count":206,"range":31,"status":0},{"index":46,"keyword":"搜索关键词-45","count":355,"range":5,"status":1},{"index":47,"keyword":"搜索关键词-46","count":261,"range":88,"status":0},{"index":48,"keyword":"搜索关键词-47","count":387,"range":35,"status":1},{"index":49,"keyword":"搜索关键词-48","count":270,"range":62,"status":1},{"index":50,"keyword":"搜索关键词-49","count":791,"range":4,"status":0}],"offlineData":[{"name":"Stores 0","cvr":0.4},{"name":"Stores 1","cvr":0.5},{"name":"Stores 2","cvr":0.9},{"name":"Stores 3","cvr":0.4},{"name":"Stores 4","cvr":0.9},{"name":"Stores 5","cvr":0.3},{"name":"Stores 6","cvr":0.5},{"name":"Stores 7","cvr":0.3},{"name":"Stores 8","cvr":0.3},{"name":"Stores 9","cvr":0.7}],"offlineChartData":[{"x":1539085892843,"y1":69,"y2":102},{"x":1539087692843,"y1":72,"y2":57},{"x":1539089492843,"y1":37,"y2":101},{"x":1539091292843,"y1":31,"y2":15},{"x":1539093092843,"y1":103,"y2":71},{"x":1539094892843,"y1":100,"y2":35},{"x":1539096692843,"y1":104,"y2":69},{"x":1539098492843,"y1":41,"y2":47},{"x":1539100292843,"y1":106,"y2":109},{"x":1539102092843,"y1":32,"y2":12},{"x":1539103892843,"y1":37,"y2":96},{"x":1539105692843,"y1":50,"y2":58},{"x":1539107492843,"y1":85,"y2":83},{"x":1539109292843,"y1":87,"y2":102},{"x":1539111092843,"y1":105,"y2":12},{"x":1539112892843,"y1":88,"y2":109},{"x":1539114692843,"y1":55,"y2":24},{"x":1539116492843,"y1":20,"y2":13},{"x":1539118292843,"y1":47,"y2":31},{"x":1539120092843,"y1":46,"y2":107}],"salesTypeData":[{"x":"家用电器","y":4544},{"x":"食用酒水","y":3321},{"x":"个护健康","y":3113},{"x":"服饰箱包","y":2341},{"x":"母婴产品","y":1231},{"x":"其他","y":1231}],"salesTypeDataOnline":[{"x":"家用电器","y":244},{"x":"食用酒水","y":321},{"x":"个护健康","y":311},{"x":"服饰箱包","y":41},{"x":"母婴产品","y":121},{"x":"其他","y":111}],"salesTypeDataOffline":[{"x":"家用电器","y":99},{"x":"食用酒水","y":188},{"x":"个护健康","y":344},{"x":"服饰箱包","y":255},{"x":"其他","y":65}],"radarData":[{"name":"个人","label":"引用","value":10},{"name":"个人","label":"口碑","value":8},{"name":"个人","label":"产量","value":4},{"name":"个人","label":"贡献","value":5},{"name":"个人","label":"热度","value":7},{"name":"团队","label":"引用","value":3},{"name":"团队","label":"口碑","value":9},{"name":"团队","label":"产量","value":6},{"name":"团队","label":"贡献","value":3},{"name":"团队","label":"热度","value":1},{"name":"部门","label":"引用","value":4},{"name":"部门","label":"口碑","value":1},{"name":"部门","label":"产量","value":6},{"name":"部门","label":"贡献","value":5},{"name":"部门","label":"热度","value":7}]}"""

  val tags =
    """{"list":[{"name":"承德市","value":82,"type":2},{"name":"鞍山市","value":95,"type":0},{"name":"攀枝花市","value":10,"type":2},{"name":"三沙市","value":49,"type":2},{"name":"马鞍山市","value":81,"type":2},{"name":"日喀则地区","value":16,"type":1},{"name":"沧州市","value":46,"type":2},{"name":"牡丹江市","value":5,"type":1},{"name":"莆田市","value":40,"type":1},{"name":"澳门半岛","value":44,"type":0},{"name":"上海市","value":98,"type":1},{"name":"离岛","value":49,"type":0},{"name":"上海市","value":81,"type":2},{"name":"淮安市","value":92,"type":0},{"name":"阿里地区","value":73,"type":1},{"name":"汉中市","value":15,"type":0},{"name":"马鞍山市","value":50,"type":0},{"name":"淮安市","value":43,"type":1},{"name":"荆州市","value":66,"type":1},{"name":"大同市","value":37,"type":2},{"name":"咸阳市","value":36,"type":1},{"name":"晋城市","value":93,"type":2},{"name":"和田地区","value":42,"type":2},{"name":"七台河市","value":49,"type":1},{"name":"巴中市","value":55,"type":1},{"name":"泸州市","value":93,"type":1},{"name":"渭南市","value":19,"type":0},{"name":"淄博市","value":64,"type":2},{"name":"喀什地区","value":85,"type":1},{"name":"湖州市","value":75,"type":1},{"name":"铜仁市","value":13,"type":1},{"name":"临夏回族自治州","value":19,"type":2},{"name":"鞍山市","value":85,"type":1},{"name":"阿克苏地区","value":81,"type":2},{"name":"蚌埠市","value":4,"type":1},{"name":"海北藏族自治州","value":46,"type":0},{"name":"资阳市","value":5,"type":1},{"name":"遵义市","value":12,"type":1},{"name":"邯郸市","value":31,"type":2},{"name":"云林县","value":51,"type":2},{"name":"来宾市","value":24,"type":1},{"name":"汕头市","value":30,"type":1},{"name":"承德市","value":29,"type":0},{"name":"那曲地区","value":38,"type":2},{"name":"无锡市","value":6,"type":0},{"name":"上海市","value":16,"type":1},{"name":"沧州市","value":89,"type":1},{"name":"上海市","value":56,"type":0},{"name":"益阳市","value":87,"type":2},{"name":"泰安市","value":50,"type":1},{"name":"吕梁市","value":71,"type":1},{"name":"上海市","value":45,"type":2},{"name":"漯河市","value":48,"type":1},{"name":"孝感市","value":84,"type":1},{"name":"南宁市","value":80,"type":0},{"name":"通辽市","value":52,"type":1},{"name":"香港岛","value":14,"type":2},{"name":"承德市","value":15,"type":2},{"name":"绍兴市","value":63,"type":1},{"name":"湘西土家族苗族自治州","value":5,"type":0},{"name":"新界","value":12,"type":2},{"name":"南通市","value":53,"type":0},{"name":"郴州市","value":19,"type":1},{"name":"福州市","value":100,"type":2},{"name":"黑河市","value":74,"type":2},{"name":"漳州市","value":18,"type":0},{"name":"云林县","value":32,"type":2},{"name":"玉树藏族自治州","value":5,"type":0},{"name":"苗栗县","value":68,"type":1},{"name":"拉萨市","value":3,"type":2},{"name":"沧州市","value":40,"type":2},{"name":"巢湖市","value":87,"type":1},{"name":"吉林市","value":38,"type":0},{"name":"吐鲁番地区","value":40,"type":1},{"name":"天水市","value":4,"type":1},{"name":"萍乡市","value":98,"type":1},{"name":"克孜勒苏柯尔克孜自治州","value":17,"type":1},{"name":"通化市","value":28,"type":1},{"name":"嘉兴市","value":33,"type":1},{"name":"营口市","value":89,"type":1},{"name":"包头市","value":15,"type":2},{"name":"海口市","value":27,"type":1},{"name":"博尔塔拉蒙古自治州","value":29,"type":1},{"name":"黔西南布依族苗族自治州","value":97,"type":2},{"name":"佛山市","value":36,"type":1},{"name":"怀化市","value":19,"type":1},{"name":"吉林市","value":70,"type":1},{"name":"蚌埠市","value":42,"type":1},{"name":"信阳市","value":43,"type":2},{"name":"铜仁市","value":68,"type":1},{"name":"舟山市","value":2,"type":1},{"name":"昭通市","value":8,"type":0},{"name":"南阳市","value":87,"type":1},{"name":"丽水市","value":72,"type":2},{"name":"台南市","value":82,"type":2},{"name":"哈密地区","value":75,"type":1},{"name":"丽江市","value":85,"type":2},{"name":"随州市","value":5,"type":0},{"name":"渭南市","value":97,"type":2},{"name":"银川市","value":49,"type":1}]}"""

  val activities =
    """[{"id":"trend-1","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"曲丽丽","avatar":"https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png"},"group":{"name":"高逼格设计天团","link":"http://github.com/"},"project":{"name":"六月迭代","link":"http://github.com/"},"template":"在 @{group} 新建项目 @{project}"},{"id":"trend-2","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"付小小","avatar":"https://gw.alipayobjects.com/zos/rmsportal/cnrhVkzwxjPwAaCfPbdc.png"},"group":{"name":"高逼格设计天团","link":"http://github.com/"},"project":{"name":"六月迭代","link":"http://github.com/"},"template":"在 @{group} 新建项目 @{project}"},{"id":"trend-3","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"林东东","avatar":"https://gw.alipayobjects.com/zos/rmsportal/gaOngJwsRYRaVAuXXcmB.png"},"group":{"name":"中二少女团","link":"http://github.com/"},"project":{"name":"六月迭代","link":"http://github.com/"},"template":"在 @{group} 新建项目 @{project}"},{"id":"trend-4","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"周星星","avatar":"https://gw.alipayobjects.com/zos/rmsportal/WhxKECPNujWoWEFNdnJE.png"},"project":{"name":"5 月日常迭代","link":"http://github.com/"},"template":"将 @{project} 更新至已发布状态"},{"id":"trend-5","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"朱偏右","avatar":"https://gw.alipayobjects.com/zos/rmsportal/ubnKSIfAJTxIgXOKlciN.png"},"project":{"name":"工程效能","link":"http://github.com/"},"comment":{"name":"留言","link":"http://github.com/"},"template":"在 @{project} 发布了 @{comment}"},{"id":"trend-6","updatedAt":"2018-10-09T11:39:10.580Z","user":{"name":"乐哥","avatar":"https://gw.alipayobjects.com/zos/rmsportal/jZUIxmJycoymBprLOUbT.png"},"group":{"name":"程序员日常","link":"http://github.com/"},"project":{"name":"品牌迭代","link":"http://github.com/"},"template":"在 @{group} 新建项目 @{project}"}]"""

  val functionFakeList =
    """function fakeList(count) {
      |  const list = [];
      |  for (i = 0; i < count; i += 1) {
      |    list.push({
      |      id: 'fake-list-' + i,
      |      owner: user[i % 10],
      |      title: titles[i % 8],
      |      avatar: avatars[i % 8],
      |      cover: parseInt(i / 4, 10) % 2 === 0 ? covers[i % 4] : covers[3 - (i % 4)],
      |      status: ['active', 'exception', 'normal'][i % 3],
      |      percent: Math.ceil(Math.random() * 50) + 50,
      |      logo: avatars[i % 8],
      |      href: 'https://ant.design',
      |      updatedAt: new Date(new Date().getTime() - 1000 * 60 * 60 * 2 * i),
      |      createdAt: new Date(new Date().getTime() - 1000 * 60 * 60 * 2 * i),
      |      subDescription: desc[i % 5],
      |      description:
      |        '在中台产品的研发过程中，会出现不同的设计规范和实现方式，但其中往往存在很多类似的页面和组件，这些类似的组件会被抽离成一套标准规范。',
      |      activeUser: Math.ceil(Math.random() * 100000) + 100000,
      |      newUser: Math.ceil(Math.random() * 1000) + 1000,
      |      star: Math.ceil(Math.random() * 100) + 100,
      |      like: Math.ceil(Math.random() * 100) + 100,
      |      message: Math.ceil(Math.random() * 10) + 10,
      |      content:
      |        '段落示意：蚂蚁金服设计平台 ant.design，用最小的工作量，无缝接入蚂蚁金服生态，提供跨越设计与开发的体验解决方案。蚂蚁金服设计平台 ant.design，用最小的工作量，无缝接入蚂蚁金服生态，提供跨越设计与开发的体验解决方案。',
      |      members: [
      |        {
      |          avatar: 'https://gw.alipayobjects.com/zos/rmsportal/ZiESqWwCXBRQoaPONSJe.png',
      |          name: '曲丽丽',
      |          id: 'member1',
      |        },
      |        {
      |          avatar: 'https://gw.alipayobjects.com/zos/rmsportal/tBOxZPlITHqwlGjsJWaF.png',
      |          name: '王昭君',
      |          id: 'member2',
      |        },
      |        {
      |          avatar: 'https://gw.alipayobjects.com/zos/rmsportal/sBxjgqiuHMGRkIjqlQCd.png',
      |          name: '董娜娜',
      |          id: 'member3',
      |        },
      |      ],
      |    });
      |  }
      |  return list;
      |}""".stripMargin

}
