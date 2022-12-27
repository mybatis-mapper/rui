package ${package};

import ${package.parent.parent}.model.${it.name.className};
import ${package.parent}.BaseService;
import ${package.parent}.${it.name.className}Service;
import org.springframework.stereotype.Service;

/**
* ${it.comment}
*/
@Service
public class ${it.name.className}ServiceImpl extends BaseService<${it.name.className}> implements ${it.name.className}Service {

}
