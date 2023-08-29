#!/usr/bin/env python3

from azure.cli.core import get_default_cli
from pathlib import Path
from os import environ
from multiprocessing import Process
import string
import random


cli: any = get_default_cli()

dataSource: str = environ['DATA_SOURCE']

namespaces: list[str] = ["AppPlatform", "Cache", "CognitiveServices", "DBforPostgreSQL", "Insights", "OperationalInsights", "SaaS", "ServiceLinker"]

currentDir: Path = Path.cwd()
root: Path = currentDir.parent.parent.parent
appsPath: Path = root / "apps" 

resources: Path = currentDir.parent.parent / "resources" / "json"
alaResources: Path = resources / "ala"
deployResources: Path = resources / "deploy" 
routeResources: Path = resources / "routes"  
tbsResources: Path = resources / "tbs" 

CART_SERVICE_REDIS_CONNECTION: str = "cart_service_cache"
ORDER_SERVICE_POSTGRES_CONNECTION: str = "order_service_db"
CATALOG_SERVICE_DB_CONNECTION: str = "catalog_service_db"
ACMEFIT_CATALOG_DB_NAME: str = "acmefit_catalog"
ACMEFIT_ORDER_DB_NAME: str = "acmefit_order"
ACMEFIT_POSTGRES_DB_PASSWORD: str = "Acm3F!tness"
ACMEFIT_POSTGRES_DB_USER: str = "dbadmin"
APPLICATION_ID: str = ""
CONFIG_REPO: str = "https://github.com/Azure-Samples/acme-fitness-store-config"
dbConnections: dict[str, str] = {
                                'order': ORDER_SERVICE_POSTGRES_CONNECTION,
                                'catalog': CATALOG_SERVICE_DB_CONNECTION
                             }
databases: dict[str, str] = {
                            'order': ACMEFIT_ORDER_DB_NAME,
                            'catalog': ACMEFIT_CATALOG_DB_NAME
                         }
services: dict[str, str] = {
                            'cart': 'cart-service',
                            'identity': 'identity-service',
                            'order': 'order-service',
                            'payment': 'payment-service',
                            'catalog': 'catalog-service',
                            'shopping': 'frontend'
                        }

jwk_set_uri = ""
gateway_url = ""
app_insights_key = ""

CUSTOM_BUILDER: str = "no-bindings-builder"

with open(dataSource) as data:
    students = [
                dict
                (
                    zip(['user', 'pass', 'sub', 'rg', 'region', 'suffix', 'tenantId'], line.strip().split(','))
                )
                for line in data
             ]


def accept_terms(student) -> None:
    for name in namespaces:
        print(f"enabling {name} for sub {student['sub']}")
        cli.invoke(
            [
                'provider',
                'register',
                '--namespace',
                'Microsoft.'+name,
                '--subscription',
                student['sub']
            ]
        )
    print(f"accepting terms for ASAe for sub {student['sub']}")
    get_default_cli().invoke(
        [
            'term', 
            'accept', 
            '--publisher',
            'vmware-inc', 
            '--product', 
            'azure-spring-cloud-vmware-tanzu-2', 
            '--plan',
            'asa-ent-hr-mtr',
            '--only-show-errors',
            '-onone',
            '--subscription', 
            student['sub']
        ]
    )


def create_rgs(student) -> None:
    print(f"creating resource group {student['rg']} for subscription f{student['sub']}")
    cli.invoke(
        [
            'group', 
            'create', 
            '--name',
            student['rg'],
            '--location',
            student['region'],
            '--subscription', 
            student['sub']
        ]
    )

def create_asa_e(student) -> None:
    service_name = f'{student["rg"]}-asa'
    logs_settings: str = str(alaResources.joinpath("logs").with_suffix(".json"))
    metrics_setting: str = str(str(alaResources.joinpath("metrics").with_suffix(".json")))
    print(f"creating ASAe instance in resource-group {student['rg']} named {student['rg']}-asa")
    cli.invoke(
        [
            'spring',
            'create',
            '--name',
            service_name,
            '--resource-group',
            student['rg'],
            '--location',
            student['region'],
            '--subscription', 
            student['sub'],
            '--sku',
            'Enterprise',
            '--enable-application-configuration-service',
            '--enable-service-registry',
            '--enable-gateway', 
            '--enable-api-portal',
            '--enable-alv',
            '--enable-app-acc',
            '--build-pool-size',
            'S2'
        ]
    )



    print(f'enabling Azure Log Analytics')
    cli.invoke(
        [
            'monitor',
            'log-analytics',
            'workspace',
            'create',
            '--workspace-name',
            f'{student["rg"]}-la',
            '--resource-group',
            student['rg'],
            '--location',
            student['region'],
            '--subscription',
            student['sub']
        ]
    )

    cli.invoke(
        [
            'monitor',
            'log-analytics',
            'workspace',
            'show',
            '--resource-group',
            student["rg"],
            '--subscription',
            student['sub'],
            '--workspace-name',
            f'{student["rg"]}-la'
        ]
    )
    log_analytics_resource_id = cli.result.result['id']

    cli.invoke(
        [
            'spring',
            'show',
            '--name',
            service_name,
            '--resource-group',
            student["rg"],
            '--subscription',
            student['sub']
        ]
    )
    spring_apps_resource_id = cli.result.result['id']
    cli.invoke(
        [
            'monitor',
            'diagnostic-settings',
            'create',
            '--name',
            'send-logs-and-metrics-to-log-analytics',
            '--resource',
            spring_apps_resource_id,
            '--workspace',
            log_analytics_resource_id,
            '--logs',
            f'@{logs_settings}',
            '--metrics',
            f'@{metrics_setting}',
            '--subscription',
            student['sub']
        ]
    )
    cli.invoke(
        [
            'spring',
            'dev-tool',
            'create',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            '--assign-endpoint'
        ]
    )


def create_dependencies(student) -> None:
    print("creating deps")
    redis_name: str = f'{student["rg"]}-redis'
    # suffix: str = ''.join(random.choices(string.ascii_lowercase + string.digits, k=3))
    acmefit_postgres_server: str = f'{student["rg"]}-postgres'
    print(f"Creating Azure Cache for Redis Instance {redis_name} in location {student['region']}")
    args = [
            'redis',
            'create',
            '--name',
            redis_name,
            '--resource-group',
            student['rg'],
            '--location',
            student['region'],
            '--subscription',
            student['sub'],
            '--sku',
            'Basic',
            '--vm-size',
            'c0'
        ]
    print(f'args for redis: {args}')
    cli.invoke(args)
    print(f"Creating Azure Database for Postgres {acmefit_postgres_server}")
    cli.invoke(
        [
            'postgres',
            'flexible-server',
            'create',
            '--name',
            acmefit_postgres_server,
            '--resource-group',
            student['rg'],
            '--location',
            student['region'],
            '--subscription',
            student['sub'],
            '--admin-user',
            ACMEFIT_POSTGRES_DB_USER,
            '--admin-password',
            ACMEFIT_POSTGRES_DB_PASSWORD,
            '--public-access',
            '0.0.0.0',
            '--tier',
            'Burstable',
            '--sku-name',
            'Standard_B1ms',
            '--version',
            '14',
            '--storage-size',
            '32'
        ]
    )
    print("setting up firewall rules")
    cli.invoke([
        'postgres',
        'flexible-server',
        'firewall-rule',
        'create',
        '--rule-name',
        'allAzureIPs',
        '--name',
        acmefit_postgres_server,
        '--resource-group',
        student['rg'],
        '--subscription',
        student['sub'],
        '--start-ip-address',
        '0.0.0.0',
        '--end-ip-address',
        '0.0.0.0'
    ])

    print(f"Enabling AD auth on {acmefit_postgres_server}")
    cli.invoke(
        [
            'postgres',
            'flexible-server',
            'parameter',
            'set',
            '--server-name',
            acmefit_postgres_server,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            '--name',
            'azure.extensions',
            '--value',
            'uuid-ossp'
        ]
    )
    for database in databases:
        print(f"Creating Postgres Database {databases[database]}")
        cli.invoke(
            [
                'postgres',
                'flexible-server',
                'db',
                'create',
                '--server-name',
                acmefit_postgres_server,
                '--resource-group',
                student['rg'],
                '--subscription',
                student['sub'],
                '--database-name',
                databases[database]
            ]
        )


def create_builder(student) -> None:
    service_name = f'{student["rg"]}-asa'
    resource_path = str(tbsResources.joinpath("builder").with_suffix(".json"))
    print(f"Creating a custom builder with name {CUSTOM_BUILDER} and configuration {resource_path}")
    cli.invoke(
        [
            'spring',
            'build-service',
            'builder',
            'create',
            '--name',
            CUSTOM_BUILDER,
            '--resource-group',
            student['rg'],
            '--service',
            service_name,
            '--subscription',
            student['sub'],
            '--builder-file',
            resource_path,
            '--no-wait'
        ]
    )


def configure_acs(student) -> None:
    service_name = f'{student["rg"]}-asa'

    print("configuring acs")
    cli.invoke(
        [
            'spring',
            'application-configuration-service',
            'git',
            'repo',
            'add',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            '--name',
            'acme-config',
            '--label',
            'main',
            '--patterns',
            'catalog,identity/default,payment',
            '--uri',
            CONFIG_REPO
        ]
    )


def configure_sso_and_gateway(student) -> None:
    print(f"configuring sso and gw for {student['user']} ")
    service_name = f'{student["rg"]}-asa'

    # *** Configure SSO *** #
    azure_ad_app_name: str = f'acme-fitness-ad-{student["rg"]}'
    print(f'azure_ad_app_name: {azure_ad_app_name}')
    # ad.json
    cli.invoke(
        [
            'ad',
            'app',
            'create',
            '--display-name',
            azure_ad_app_name,
        ]
    )
    app_id = cli.result.result['appId']

    # sso.json
    cli.invoke(
        [
            'ad',
            'app',
            'credential',
            'reset',
            '--id',
            app_id,
            '--append'
        ]
    )
    
    scope = "openid,profile"
    client_id: any = cli.result.result['appId']
    client_secret: any = cli.result.result['password']

    issuer_uri: str = f"https://login.microsoftonline.com/{student['tenantId']}/v2.0"
    global jwk_set_uri
    jwk_set_uri = f"https://login.microsoftonline.com/{student['tenantId']}/discovery/v2.0/keys"

    print(f'issuer_uri: {issuer_uri}')
    print(f'jwk_set_url: {jwk_set_uri}')

    cli.invoke(
        [
            'ad',
            'sp',
            'create',
            '--id',
            app_id
        ]
    )
    # *** end Configure SSO *** #
    # *** Configure Gateway *** #
    cli.invoke(
        [
            'spring',
            'gateway',
            'update',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--assign-endpoint',
            'true',
            '--subscription',
            student['sub']
        ]
    )

    cli.invoke(
        [
            'spring',
            'gateway',
            'show',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub']
        ]
    )
    global gateway_url
    gateway_url = cli.result.result['properties']['url']
    print(f'gateway_url: {gateway_url}')
    print("updating gw - adding info")
    args = [
            'spring',
            'gateway',
            'update',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            '--api-description',
            'ACME Fitness API',
            '--api-title',
            'ACME Fitness',
            '--api-version',
            'v.01',
            '--server-url',
            f'https://{gateway_url}',
            '--allowed-origins',
            '*',
            '--client-id',
            client_id,
            '--client-secret',
            client_secret,
            '--scope',
            scope,
            '--issuer-uri',
            issuer_uri
        ]
    print(args)
    cli.invoke(args)
    # *** end configure gateway *** #
    # *** update sso portal urls *** #
    print("getting sso portal urls")
    cli.invoke(
        [
            'spring',
            'api-portal',
            'show',
            '--service',
            service_name,
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
        ]
    )

    redirect_list = f'https://{gateway_url}/login/oauth2/code/sso'
    portal_url = cli.result.result['properties']['url']
    if portal_url:
        redirect_list = redirect_list + " " + f'https://{portal_url}/oauth2-redirect.html'
        redirect_list = redirect_list + " " + f'https://{portal_url}/login/oauth2/code/sso'

    print(f'redirect list: {redirect_list}')
    print("updating redirects")
    args = [
            'ad',
            'app',
            'update',
            '--id',
            app_id,
            '--web-redirect-uris',
            redirect_list
            ]
    print(args)
    cli.invoke(args)
    # *** end update sso portal urls *** #


def create_applications(student) -> None:
    print("creating asae application slots")
    service_name = f'{student["rg"]}-asa'

    print(f"creating applications for {student['user']}")
    redis_name: str = f'{student["rg"]}-redis'
    acmefit_postgres_server: str = f'{student["rg"]}-postgres'

    for service in services:
        # create app slots
        print(f"creating app slot for {services[service]}")
        cli.invoke(
            [
                'spring',
                'app',
                'create',
                '--name',
                services[service],
                '--resource-group',
                student['rg'],
                '--service',
                service_name,
                '--subscription',
                student['sub']
            ]
        )
        # configure routes
        if 'payment' not in service:
            print(f"configuring route for {services[service]}")
            cli.invoke(
                [
                    'spring',
                    'gateway',
                    'route-config',
                    'create',
                    '--resource-group',
                    student['rg'],
                    '--service',
                    service_name,
                    '--subscription',
                    student['sub'],
                    '--name',
                    services[service],
                    '--app-name',
                    services[service],
                    '--routes-file',
                    str(routeResources.joinpath(str(services[service])).with_suffix(".json"))
                ]
            )
        # configure redis connection
        if 'cart' in service:
            print("configuring redis connection for cart")
            args = [
                    'spring',
                    'connection',
                    'create',
                    'redis',
                    '--service',
                    service_name,
                    '--resource-group',
                    student['rg'],
                    '--target-resource-group',
                    student['rg'],
                    '--server',
                    redis_name,
                    '--database',
                    '0',
                    '--app',
                    services[service],
                    '--client-type',
                    'python',
                    '--connection',
                    CART_SERVICE_REDIS_CONNECTION,
                    '--secret',
                    '--subscription',
                    student['sub']
                ]
            print(args)
            cli.invoke(args)
        if ('payment' in service) or ('catalog' in service):
            cli.invoke(
                [
                    'spring',
                    'service-registry',
                    'bind',
                    '--app',
                    services[service],
                    '--service',
                    service_name,
                    '--resource-group',
                    student['rg'],
                    '--subscription',
                    student['sub']
                ]
            )
        if ('identity' in service) or ('payment' in service) or ('catalog' in service):
            cli.invoke(
                [
                    'spring',
                    'application-configuration-service',
                    'bind',
                    '--app',
                    services[service],
                    '--service',
                    service_name,
                    '--resource-group',
                    student['rg'],
                    '--subscription',
                    student['sub']
                ]
            )
        if 'order' in service:
            print("attempting to configure order service")
            args = [
                    'spring',
                    'connection',
                    'create',
                    'postgres-flexible',
                    '--resource-group',
                    student['rg'],
                    '--service',
                    f'{student["rg"]}-asa',
                    '--connection',
                    dbConnections[service],
                    '--app',
                    services[service],
                    '--deployment',
                    'default',
                    '--target-resource-group',
                    student['rg'],
                    '--server',
                    acmefit_postgres_server,
                    '--database',
                    databases[service],
                    '--subscription',
                    student['sub'],
                    '--secret',
                    f'name={ACMEFIT_POSTGRES_DB_USER}',
                    f'secret={ACMEFIT_POSTGRES_DB_PASSWORD}',
                    '--client-type',
                    'dotnet'
                ]
            print(f"arguments for order service {args}")
            cli.invoke(args)
        if 'catalog' in service:
            cli.invoke(
                [
                    'spring',
                    'connection',
                    'create',
                    'postgres-flexible',
                    '--resource-group',
                    student['rg'],
                    '--service',
                    f'{student["rg"]}-asa',
                    '--connection',
                    dbConnections[service],
                    '--app',
                    services[service],
                    '--deployment',
                    'default',
                    '--target-resource-group',
                    student['rg'],
                    '--server',
                    acmefit_postgres_server,
                    '--database',
                    databases[service],
                    '--system-identity',
                    '--client-type',
                    'springboot',
                    '--subscription',
                    student['sub']
                ]
            )


def deploy_cart_service(student) -> None:
    print("deploying cart-service")
    cli.invoke(
        [
            'spring',
            'connection',
            'show',
            '--resource-group',
            student['rg'],
            '--service',
            f'{student["rg"]}-asa',
            '--subscription',
            student['sub'],
            '--deployment',
            'default',
            '--app',
            services['cart'],
            '--connection',
            CART_SERVICE_REDIS_CONNECTION
        ]
    )
    redis_connection_string = cli.result.result['configurations'][0]['value']
    args = [
            'spring',
            'build-service',
            'builder',
            'buildpack-binding',
            'show',
            '-n',
            'default',
            '--resource-group',
            student['rg'],
            '--service',
            f'{student["rg"]}-asa',
            '--subscription',
            student['sub']
        ]
    print(f'args for app_insights_key {args}')
    cli.invoke(args)
    data = cli.result.result
    print(f'app_insights_key results {data}')
    global app_insights_key
    app_insights_key = data['properties']['launchProperties']['properties']
    if 'connection-string' in app_insights_key:
        app_insights_key = app_insights_key['connection-string']
    if 'connection_string' in app_insights_key:
        app_insights_key = app_insights_key['connection_string']
    cli.invoke(
        [
            'spring',
            'app',
            'deploy',
            '--name',
            services['cart'],
            '--builder',
            CUSTOM_BUILDER,
            '--env',
            'CART_PORT=8080',
            f'REDIS_CONNECTIONSTRING={redis_connection_string}',
            f'AUTH_URL=https://{gateway_url}',
            f'INSTRUMENTATION_KEY={app_insights_key}',
            '--service',
            f'{student["rg"]}-asa',
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            "--no-wait",
            '--source-path',
            str(appsPath.joinpath("acme-cart"))
        ]
    )


def deploy_order_service(student) -> None:
    print("deploying order-service")
    cli.invoke(
        [
            'spring',
            'connection',
            'show',
            '--resource-group',
            student['rg'],
            '--service',
            f'{student["rg"]}-asa',
            '--subscription',
            student['sub'],
            '--deployment',
            'default',
            '--app',
            services['order'],
            '--connection',
            ORDER_SERVICE_POSTGRES_CONNECTION
        ]
    )
    postgre_connect_string = cli.result.result['configurations'][0]['value']

    args = [
            'spring',
            'app',
            'deploy',
            '--name',
            services['order'],
            '--builder',
            CUSTOM_BUILDER,
            '--env',
            'DatabaseProvider=Postgres',
            f'ConnectionStrings__OrderContext={postgre_connect_string};Trust Server Certificate=true;',
            f'AcmeServiceSettings__AuthUrl=https://{gateway_url}'
            f'ApplicationInsights__ConnectionString={app_insights_key}',
            '--service',
            f'{student["rg"]}-asa',
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            "--no-wait",
            '--source-path',
            str(appsPath.joinpath("acme-order"))
        ]
    print(f'args for deploying acme-order {args}')
    cli.invoke(args)


def deploy_frontend_app(student) -> None:
    print("deploying frontend application")
    cli.invoke(
        [
            'spring',
            'app',
            'deploy',
            '--name',
            services['shopping'],
            '--builder',
            CUSTOM_BUILDER,
            '--env',
            f'APPLICATIONINSIGHTS_CONNECTION_STRING={app_insights_key}',
            '--service',
            f'{student["rg"]}-asa',
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            "--no-wait",
            '--source-path',
            str(appsPath.joinpath("acme-shopping"))
        ]
    )


def deploy_catalog_service(student) -> None:
    print("deploying catalog service")
    args = [
            'spring',
            'app',
            'deploy',
            '--name',
            services['catalog'],
            '--config-file-pattern',
            'catalog',
            '--env',
            'SPRING_DATASOURCE_AZURE_PASSWORDLESSENABLED=true',
            '--service',
            f'{student["rg"]}-asa',
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            '--no-wait',
            '--source-path',
            str(appsPath.joinpath("acme-catalog")),
            '--build-env',
            'BP_JVM_VERSION=17',
        ]
    print(args)
    cli.invoke(args)


def deploy_payment_service(student) -> None:
    print('deploying payment service')
    cli.invoke(
        [
            'spring',
            'app',
            'deploy',
            '--name',
            services['payment'],
            '--config-file-pattern',
            'payment',
            '--service',
            f'{student["rg"]}-asa',
            '--resource-group',
            student['rg'],
            '--subscription',
            student['sub'],
            "--no-wait",
            '--source-path',
            str(appsPath.joinpath("acme-payment")),
            '--build-env',
            'BP_JVM_VERSION=17'
        ]
    )


def deploy_identity_service(student) -> None:
    # f'SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI={jwk_set_uri}',
    jwk_set_uri = f"https://login.microsoftonline.com/{student['tenantId']}/discovery/v2.0/keys"
    print("deploying identity service--")
    args = [
        'spring',
        'app',
        'deploy',
        '--name',
        services['identity'],
        '--env',
        f'JWK_URI={jwk_set_uri}',
        '--config-file-pattern',
        'identity/default',
        '--service',
        f'{student["rg"]}-asa',
        '--resource-group',
        student['rg'],
        '--subscription',
        student['sub'],
        "--no-wait",
        '--source-path',
        str(appsPath.joinpath("acme-identity")),
        '--build-env',
        'BP_JVM_VERSION=17',
    ]
    print(args)
    cli.invoke(args)


if __name__ == '__main__':
    print("-----")
    for student in students:
        accept_terms(student)
        # create_spring_cloud
        create_rgs(student)
        create_asa_e(student)
        configure_acs(student)
        create_dependencies(student)
        # end create_spring_cloud
        create_builder(student)
        configure_sso_and_gateway(student)
        create_applications(student)
        d1 = Process(deploy_identity_service(student))
        d2 = Process(deploy_cart_service(student))
        d3 = Process(deploy_order_service(student))
        d4 = Process(deploy_payment_service(student))
        d5 = Process(deploy_catalog_service(student))
        d6 = Process(deploy_frontend_app(student))
        d1.start()
        d2.start()
        d3.start()
        d4.start()
        d5.start()
        d6.start()
        d1.join()
        d2.join()
        d3.join()
        d4.join()
        d5.join()
        d6.join()